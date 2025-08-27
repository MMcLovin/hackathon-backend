package com.hackathon.hackathon_backend.services;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hackathon.hackathon_backend.dtos.request.SimulacaoRequestDTO;
import com.hackathon.hackathon_backend.dtos.response.*;
import com.hackathon.hackathon_backend.exceptions.ProdutoNotFoundException;
import com.hackathon.hackathon_backend.models.remote.Produto;
import com.hackathon.hackathon_backend.models.local.Simulacao;
import com.hackathon.hackathon_backend.models.local.Telemetria;
import com.hackathon.hackathon_backend.repositories.remote.ProdutoRepository;
import com.hackathon.hackathon_backend.repositories.local.SimulacaoRepository;
import com.hackathon.hackathon_backend.repositories.local.TelemetriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SimulacaoService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    @Autowired
    private EventHubProducerClient eventHubProducerClient;

    @Autowired
    private TelemetriaRepository telemetriaRepository;

    public ResultadoSimulacao calcularSAC(double valor, int prazo, double taxaJuros){
        double saldoDevedor = valor;
        double amortizacao = valor / prazo;
        List<Parcela> parcelas = new ArrayList<>();

        for(int i = 1; i <= prazo; i++){
            double juros = saldoDevedor * taxaJuros;
            double prestacao = amortizacao + juros;
            saldoDevedor -= amortizacao;

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(Math.round(amortizacao * 100.0) / 100.0);
            parcela.setValorJuros(Math.round(juros * 100.0) / 100.0);
            parcela.setValorPrestacao(Math.round(prestacao * 100) / 100.0);
            parcelas.add(parcela);
        }

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setTipo("SAC");
        resultado.setParcelas(parcelas);
        return resultado;
    }

    public ResultadoSimulacao calcularPRICE(double valor, int prazo, double taxaJuros) {
        double saldoDevedor = valor;
        double prestacao = valor * (Math.pow((1 + taxaJuros), prazo) * taxaJuros) / (Math.pow((1 + taxaJuros), prazo) - 1);
        List<Parcela> parcelas = new ArrayList<>();

        for (int i = 1; i <= prazo; i++) {
            double juros = saldoDevedor * taxaJuros;
            double amortizacao = prestacao - juros;
            saldoDevedor -= amortizacao;

            Parcela parcela = new Parcela();
            parcela.setNumero(i);
            parcela.setValorAmortizacao(Math.round(amortizacao * 100.0) / 100.0);
            parcela.setValorJuros(Math.round(juros * 100.0) / 100.0);
            parcela.setValorPrestacao(Math.round(prestacao * 100.0) / 100.0);
            parcelas.add(parcela);
        }

        ResultadoSimulacao resultado = new ResultadoSimulacao();
        resultado.setTipo("PRICE");
        resultado.setParcelas(parcelas);
        return resultado;
    }

    // persistindo a simulacao no banco de dados local
    public Simulacao salvarSimulacao(SimulacaoRequestDTO request, Produto produto, List<ResultadoSimulacao> resultados) {
        Simulacao simulacao = new Simulacao();
        simulacao.setCodigoProduto(produto.getCoProduto());
        simulacao.setDescricaoProduto(produto.getNoProduto());
        simulacao.setValorDesejado(request.getValorDesejado());
        simulacao.setPrazo(request.getPrazo());
        simulacao.setTaxaJuros(produto.getPcTaxaJuros());
        simulacao.setDataHoraSimulacao(LocalDateTime.now());

        try {
            String resultadosJson = objectMapper.writeValueAsString(resultados);
            simulacao.setResultadoSimulacaoJson(resultadosJson);

            // Envio para o Event Hub
            EventData eventData = new EventData(resultadosJson);
            eventHubProducerClient.send(List.of(eventData));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter resultado da simulação para JSON", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Erro ao enviar para o EventHub", e);
        }
        simulacaoRepository.save(simulacao);
        return simulacao;
    }

    // Validar e filtrar o produto que atende aos parametros recebidos
    public Produto encontrarProduto(SimulacaoRequestDTO request){
        List<Produto> produtos = produtoRepository.findAll();
        Optional<Produto> produtoEncontrado = produtos.stream()
                .filter(p -> request.getPrazo() >= p.getNuMinimoMeses() && request.getPrazo() <= Optional.ofNullable(p.getNuMaximoMeses()).orElse(Short.MAX_VALUE))
                .filter(p -> request.getValorDesejado() >= p.getVrMinimo() && request.getValorDesejado() <= Optional.ofNullable(p.getVrMaximo()).orElse(Double.MAX_VALUE))
                .findFirst();

        if (produtoEncontrado.isEmpty()) {
            throw new ProdutoNotFoundException("Nenhum produto de crédito encontrado para os parâmetros fornecidos.");
        }

        return produtoEncontrado.get();
    }

    public SimulacaoResponseDTO simularEmprestimo(SimulacaoRequestDTO request) {
        long startTime = System.currentTimeMillis();
        int httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        try {
            Produto produto = encontrarProduto(request);

            // 2. Realiza os cálculos de SAC e PRICE
            List<ResultadoSimulacao> resultados = new ArrayList<>();
            resultados.add(calcularSAC(request.getValorDesejado(), request.getPrazo(), produto.getPcTaxaJuros()));
            resultados.add(calcularPRICE(request.getValorDesejado(), request.getPrazo(), produto.getPcTaxaJuros()));

            Simulacao simulacaoSalva = salvarSimulacao(request, produto, resultados);

            // 4. Montar e retornar o DTO de resposta
            SimulacaoResponseDTO response = new SimulacaoResponseDTO();
            response.setIdSimulacao(simulacaoSalva.getIdSimulacao());
            response.setCodigoProduto(simulacaoSalva.getCodigoProduto());
            response.setDescricaoProduto(simulacaoSalva.getDescricaoProduto());
            response.setTaxaJuros(simulacaoSalva.getTaxaJuros());
            response.setResultadoSimulacao(resultados);
            httpStatusCode = HttpStatus.OK.value();
            return response;
        } catch (ProdutoNotFoundException e) {
            httpStatusCode = HttpStatus.NOT_FOUND.value();
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long tempoExecucao = endTime - startTime;
            salvarTelemetria("simularEmprestimo", tempoExecucao, httpStatusCode);
        }
    }

    private void salvarTelemetria(String endpointNome, long tempoExecucaoMs, int httpStatusCode) {
        Telemetria telemetria = new Telemetria();
        telemetria.setEndpointNome(endpointNome);
        telemetria.setTempoExecucaoMs(tempoExecucaoMs);
        telemetria.setHttpStatusCode(httpStatusCode);
        telemetria.setDataHoraRegistro(LocalDateTime.now());
        telemetriaRepository.save(telemetria);
    }

    private List<SimulacaoResumoDTO> montarRegistros(List<Simulacao> simulacoes){
        List<SimulacaoResumoDTO> registros = new ArrayList<>();

        for(Simulacao s : simulacoes)
        {
            SimulacaoResumoDTO dto = new SimulacaoResumoDTO();
            dto.setIdSimulacao(s.getIdSimulacao());
            dto.setValorDesejado(s.getValorDesejado());
            dto.setPrazo(s.getPrazo());

            // soma das parcelas
            try{
                List<ResultadoSimulacao> resultados = objectMapper.readValue(s.getResultadoSimulacaoJson(), new TypeReference<List<ResultadoSimulacao>>() {});

                double totalSac = 0.0;
                double totalPrice = 0.0;

                // Escolhe o menor valor total entre os dois sistemas
                for (ResultadoSimulacao resultado : resultados) {
                    if ("SAC".equals(resultado.getTipo())) {
                        totalSac = resultado.getParcelas().stream()
                                .mapToDouble(Parcela::getValorPrestacao)
                                .sum();
                    } else if ("PRICE".equals(resultado.getTipo())) {
                        totalPrice = resultado.getParcelas().stream()
                                .mapToDouble(Parcela::getValorPrestacao)
                                .sum();
                    }
                }
                dto.setValorTotalParcelas(Math.min(totalSac, totalPrice));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            registros.add(dto);
        }
        return registros;
    }

    // Retornanado todas as simulações realizadas.
    public SimulacaoListagemDTO listarSimulacoes(int page, int size){
        long startTime = System.currentTimeMillis();
        int httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            Page<Simulacao> simulacoesPage = simulacaoRepository.findAll(PageRequest.of(page, size));
            List<SimulacaoResumoDTO> registros = montarRegistros(simulacoesPage.getContent());
            SimulacaoListagemDTO response = new SimulacaoListagemDTO();
            response.setPagina(simulacoesPage.getNumber());
            response.setQtdRegistros((int) simulacoesPage.getTotalElements());
            response.setQtdRegistrosPagina(simulacoesPage.getNumberOfElements());
            response.setRegistros(registros);
            httpStatusCode = HttpStatus.OK.value();
            return response;
        } finally {
            long endTime = System.currentTimeMillis();
            long tempoExecucao = endTime - startTime;
            salvarTelemetria("listarSimulacoes", tempoExecucao, httpStatusCode);
        }
    }

    private SimulacaoProdutoVolumeDTO calcularVolumePorProduto(List<Simulacao> simulacoesPorProduto) {
        double totalTaxaJuros = 0.0;
        double totalValorPrestacao = 0.0;
        double totalValorDesejado = 0.0;
        double totalValorCredito = 0.0;

        String descricaoProduto = simulacoesPorProduto.get(0).getDescricaoProduto();
        Integer codigoProduto = simulacoesPorProduto.get(0).getCodigoProduto();

        for (Simulacao simulacao : simulacoesPorProduto) {
            totalTaxaJuros += simulacao.getTaxaJuros();
            totalValorDesejado += simulacao.getValorDesejado();

            try {
                List<ResultadoSimulacao> resultados = objectMapper.readValue(simulacao.getResultadoSimulacaoJson(), new TypeReference<List<ResultadoSimulacao>>() {
                });

                if (resultados != null && !resultados.isEmpty()) {
                    double totalSac = 0.0;
                    double totalPrice = 0.0;

                    for (ResultadoSimulacao resultado : resultados) {
                        if ("SAC".equals(resultado.getTipo())) {
                            totalSac = resultado.getParcelas().stream()
                                    .mapToDouble(Parcela::getValorPrestacao)
                                    .sum();
                        } else if ("PRICE".equals(resultado.getTipo())) {
                            totalPrice = resultado.getParcelas().stream()
                                    .mapToDouble(Parcela::getValorPrestacao)
                                    .sum();
                        }
                    }

                    // Escolhe o menor valor total entre os dois sistemas
                    double valorTotalSimulacao = Math.min(totalSac, totalPrice);
                    totalValorCredito += valorTotalSimulacao;
                    totalValorPrestacao += valorTotalSimulacao;
                }
            } catch (Exception e) {
                // Lidar com o erro de JSON
                e.printStackTrace();
            }
        }

        SimulacaoProdutoVolumeDTO dto = new SimulacaoProdutoVolumeDTO();
        dto.setCodigoProduto(codigoProduto);
        dto.setDescricaoProduto(descricaoProduto);
        dto.setTaxaMediaJuros(totalTaxaJuros / simulacoesPorProduto.size());
        dto.setValorMedioPrestacao(totalValorPrestacao / simulacoesPorProduto.size());
        dto.setValorTotalDesejado(totalValorDesejado);
        dto.setValorTotalCredito(totalValorCredito);

        return dto;
    }

    // retornando os valores simulados para cada produto em um dia específico
    public SimulacaoVolumeDTO calcularVolumePorDia(LocalDate dataReferencia) {
        long startTime = System.currentTimeMillis();
        int httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        try {
            // Inicializa o ObjectMapper com o módulo para datas
            objectMapper.registerModule(new JavaTimeModule());

            // Busca todas as simulações e filtra pela data de referência
            List<Simulacao> simulacoesDoDia = simulacaoRepository.findAll().stream()
                    .filter(s -> s.getDataHoraSimulacao() != null && s.getDataHoraSimulacao().toLocalDate().equals(dataReferencia))
                    .collect(Collectors.toList());

            // Agrupa as simulações por produto
            Map<Integer, List<Simulacao>> simulacoesPorProduto = simulacoesDoDia.stream()
                    .collect(Collectors.groupingBy(Simulacao::getCodigoProduto));

            // Itera sobre os grupos e calcula o volume para cada produto
            List<SimulacaoProdutoVolumeDTO> volumes = simulacoesPorProduto.values().stream()
                    .map(this::calcularVolumePorProduto)
                    .collect(Collectors.toList());

            // Monta a resposta
            SimulacaoVolumeDTO response = new SimulacaoVolumeDTO();
            response.setDataReferencia(dataReferencia);
            response.setSimulacoes(volumes);
            httpStatusCode = HttpStatus.OK.value();
            return response;
        } finally {
            long endTime = System.currentTimeMillis();
            long tempoExecucao = endTime - startTime;
            salvarTelemetria("calcularVolumePorDia", tempoExecucao, httpStatusCode);
        }
    }

    public TelemetriaDTO calcularTelemetria() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        List<Telemetria> telemetriasDoDia = telemetriaRepository.findByDataHoraRegistroBetween(inicioDoDia, fimDoDia);

        Map<String, List<Telemetria>> telemetriaPorEndpoint = telemetriasDoDia.stream()
                .collect(Collectors.groupingBy(Telemetria::getEndpointNome));

        List<TelemetriaEndpointDTO> listaEndpoints = new ArrayList<>();

        for (Map.Entry<String, List<Telemetria>> entry : telemetriaPorEndpoint.entrySet()) {
            String nomeApi = entry.getKey();
            List<Telemetria> registros = entry.getValue();

            long qtdRequisicoes = registros.size();
            long tempoMinimo = registros.stream().mapToLong(Telemetria::getTempoExecucaoMs).min().orElse(0);
            long tempoMaximo = registros.stream().mapToLong(Telemetria::getTempoExecucaoMs).max().orElse(0);
            double tempoMedio = registros.stream().mapToLong(Telemetria::getTempoExecucaoMs).average().orElse(0.0);
            long qtdSucesso = registros.stream().filter(t -> t.getHttpStatusCode() == HttpStatus.OK.value()).count();
            double percentualSucesso = qtdRequisicoes > 0 ? (double) qtdSucesso / qtdRequisicoes : 0.0;

            TelemetriaEndpointDTO dto = new TelemetriaEndpointDTO();
            dto.setNomeApi(nomeApi);
            dto.setQtdRequisicoes((int) qtdRequisicoes);
            dto.setTempoMinimo(tempoMinimo);
            dto.setTempoMaximo(tempoMaximo);
            dto.setTempoMedio(Math.round(tempoMedio));
            dto.setPercentualSucesso(percentualSucesso);

            listaEndpoints.add(dto);
        }

        TelemetriaDTO telemetriaDto = new TelemetriaDTO();
        telemetriaDto.setDataReferencia(hoje);
        telemetriaDto.setListaEndpoints(listaEndpoints);

        return telemetriaDto;
    }
}
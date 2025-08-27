package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;
import java.util.List;

@Data
public class SimulacaoResponseDTO {

    private Long idSimulacao;
    private Integer codigoProduto;
    private String descricaoProduto;
    private double taxaJuros;
    private List<ResultadoSimulacao> resultadoSimulacao;
}
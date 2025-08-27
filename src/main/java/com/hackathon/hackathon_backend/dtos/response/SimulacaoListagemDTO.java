package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;
import java.util.List;

@Data
public class SimulacaoListagemDTO {
    private int pagina;
    private int qtdRegistros;
    private int qtdRegistrosPagina;
    private List<SimulacaoResumoDTO> registros;
}

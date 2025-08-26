package com.hackathon.hackathon_backend.dtos;

import lombok.Data;
import java.util.List;

@Data
public class SimulacaoListagemDTO {
    private int pagina;
    private int qtdRegistros;
    private int qtdRegistrosPagina;
    private List<SimulacaoResumoDTO> registros;
}

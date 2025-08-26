package com.hackathon.hackathon_backend.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SimulacaoVolumeDTO {

    private LocalDate dataReferencia;
    private List<SimulacaoProdutoVolumeDTO> simulacoes;
}
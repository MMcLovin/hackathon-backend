package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;

@Data
public class SimulacaoResumoDTO {
    private Long idSimulacao;
    private double valorDesejado;
    private int prazo;
    private double valorTotalParcelas;
}

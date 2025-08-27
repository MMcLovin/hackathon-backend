package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;

@Data
public class TelemetriaEndpointDTO {

    private String nomeApi;
    private int qtdRequisicoes;
    private double tempoMedio;
    private double tempoMinimo;
    private double tempoMaximo;
    private double percentualSucesso;
}
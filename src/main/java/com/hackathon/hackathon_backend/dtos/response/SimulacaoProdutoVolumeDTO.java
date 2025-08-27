package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;

@Data
public class SimulacaoProdutoVolumeDTO {

    private Integer codigoProduto;
    private String descricaoProduto;
    private double taxaMediaJuros;
    private double valorMedioPrestacao;
    private double valorTotalDesejado;
    private double valorTotalCredito;
}
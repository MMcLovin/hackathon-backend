package com.hackathon.hackathon_backend.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SimulacaoProdutoVolumeDTO {

    private Integer codigoProduto;
    private String descricaoProduto;
    private double taxaMediaJuros;
    private double valorMedioPrestacao;
    private double valorTotalDesejado;
    private double valorTotalCredito;
}
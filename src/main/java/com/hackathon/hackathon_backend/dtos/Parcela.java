package com.hackathon.hackathon_backend.dtos;

import lombok.Data;

@Data
public class Parcela {
    private int numero;
    private double valorAmortizacao;
    private double valorJuros;
    private double valorPrestacao;
}

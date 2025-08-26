package com.hackathon.hackathon_backend.dtos;

import com.hackathon.hackathon_backend.models.local.Simulacao;
import lombok.Data;
import java.text.DateFormat;
import java.util.List;

@Data
public class SimulacaoResumoDTO {
    private Long idSimulacao;
    private double valorDesejado;
    private int prazo;
    private double valorTotalParcelas;
}

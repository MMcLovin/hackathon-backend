package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;
import java.util.List;

@Data
public class ResultadoSimulacao {
    // "SAC" ou "PRICE"
    private String tipo;
    // Lista de parcelas
    private List<Parcela> parcelas;
}

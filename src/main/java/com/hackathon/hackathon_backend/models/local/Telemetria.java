package com.hackathon.hackathon_backend.models.local;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetria")
@Data
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endpointNome;
    private long tempoExecucaoMs;
    private int httpStatusCode;
    private LocalDateTime dataHoraRegistro;
}
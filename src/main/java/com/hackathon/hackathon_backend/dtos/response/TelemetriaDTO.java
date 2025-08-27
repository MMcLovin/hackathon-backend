package com.hackathon.hackathon_backend.dtos.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TelemetriaDTO {

    private LocalDate dataReferencia;
    private List<TelemetriaEndpointDTO> listaEndpoints;
}
package com.hackathon.hackathon_backend.controller;

import com.hackathon.hackathon_backend.dtos.SimulacaoListagemDTO;
import com.hackathon.hackathon_backend.dtos.SimulacaoRequestDTO;
import com.hackathon.hackathon_backend.dtos.SimulacaoResponseDTO;
import com.hackathon.hackathon_backend.dtos.SimulacaoVolumeDTO;
import com.hackathon.hackathon_backend.dtos.TelemetriaDTO;
import com.hackathon.hackathon_backend.services.SimulacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/simulacao")
public class SimulacaoController {

    @Autowired
    private SimulacaoService simulacaoService;

    @PostMapping
    public ResponseEntity<SimulacaoResponseDTO> simular(@RequestBody SimulacaoRequestDTO request) {
        SimulacaoResponseDTO response = simulacaoService.simularEmprestimo(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/historico")
    public ResponseEntity<SimulacaoListagemDTO> listarHistorico(){
        SimulacaoListagemDTO response = simulacaoService.listarSimulacoes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/volume")
    public ResponseEntity<SimulacaoVolumeDTO> getVolumePorDia(@RequestParam("dataReferencia") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        SimulacaoVolumeDTO response = simulacaoService.calcularVolumePorDia(dataReferencia);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/telemetria")
    public ResponseEntity<TelemetriaDTO> getTelemetria() {
        TelemetriaDTO response = simulacaoService.calcularTelemetria();
        return ResponseEntity.ok(response);
    }
}

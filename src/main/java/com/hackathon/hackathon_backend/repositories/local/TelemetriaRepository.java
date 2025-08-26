package com.hackathon.hackathon_backend.repositories.local;

import com.hackathon.hackathon_backend.models.local.Telemetria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    List<Telemetria> findByDataHoraRegistroBetween(LocalDateTime start, LocalDateTime end);
}
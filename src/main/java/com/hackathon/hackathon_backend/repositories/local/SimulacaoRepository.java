package com.hackathon.hackathon_backend.repositories.local;

import com.hackathon.hackathon_backend.models.local.Simulacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {
}

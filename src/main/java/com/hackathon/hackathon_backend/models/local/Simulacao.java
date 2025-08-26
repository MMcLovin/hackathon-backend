package com.hackathon.hackathon_backend.models.local;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Adicione as novas importações
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Entity
@Table(name = "simulacao")
@Data
public class Simulacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSimulacao;

    private Integer codigoProduto;
    private String descricaoProduto;
    private Double taxaJuros;
    private Double valorDesejado;
    private Integer prazo;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private String resultadoSimulacaoJson;

    private LocalDateTime dataHoraSimulacao;
}
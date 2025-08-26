package com.hackathon.hackathon_backend.models.remote;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "PRODUTO", schema = "dbo")
@Data // Anotação do Lombok para gerar getters, setters, etc.
public class Produto {

    @Id
    @Column(name = "CO_PRODUTO")
    private int coProduto;

    @Column(name = "NO_PRODUTO")
    private String noProduto;

    @Column(name = "PC_TAXA_JUROS")
    private double pcTaxaJuros;

    @Column(name = "NU_MINIMO_MESES")
    private short nuMinimoMeses;

    @Column(name = "NU_MAXIMO_MESES")
    private Short nuMaximoMeses;

    @Column(name = "VR_MINIMO")
    private double vrMinimo;

    @Column(name = "VR_MAXIMO")
    private Double vrMaximo;
}

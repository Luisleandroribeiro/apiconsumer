package com.hidroweb.apiconsumer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estacoes")
@Getter
@Setter
public class Estacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigoestacao")
    private Long codigoEstacao;

    @Column(name = "estacao_nome")
    private String estacaoNome;

    @Column(name = "uf_estacao")
    private String ufEstacao;

    @Column(name = "municipio_nome")
    private String municipioNome;

    @Column(name = "bacia_nome")
    private String baciaNome;

    @Column(name = "sub_bacia_nome")
    private String subBaciaNome;

    @Column(name = "rio_nome")
    private String rioNome;

    @Column(name = "tipo_estacao")
    private String tipoEstacao;

}

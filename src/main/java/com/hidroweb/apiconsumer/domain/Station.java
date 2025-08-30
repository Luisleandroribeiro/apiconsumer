package com.hidroweb.apiconsumer.domain;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "estacoes")
@With
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class Station {

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
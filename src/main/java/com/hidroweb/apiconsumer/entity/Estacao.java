package com.hidroweb.apiconsumer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estacoes")
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

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCodigoEstacao() {
        return codigoEstacao;
    }

    public void setCodigoEstacao(Long codigoEstacao) {
        this.codigoEstacao = codigoEstacao;
    }

    public String getEstacaoNome() {
        return estacaoNome;
    }

    public void setEstacaoNome(String estacaoNome) {
        this.estacaoNome = estacaoNome;
    }

    public String getUfEstacao() {
        return ufEstacao;
    }

    public void setUfEstacao(String ufEstacao) {
        this.ufEstacao = ufEstacao;
    }

    public String getMunicipioNome() {
        return municipioNome;
    }

    public void setMunicipioNome(String municipioNome) {
        this.municipioNome = municipioNome;
    }

    public String getBaciaNome() {
        return baciaNome;
    }

    public void setBaciaNome(String baciaNome) {
        this.baciaNome = baciaNome;
    }

    public String getSubBaciaNome() {
        return subBaciaNome;
    }

    public void setSubBaciaNome(String subBaciaNome) {
        this.subBaciaNome = subBaciaNome;
    }

    public String getRioNome() {
        return rioNome;
    }

    public void setRioNome(String rioNome) {
        this.rioNome = rioNome;
    }

    public String getTipoEstacao() {
        return tipoEstacao;
    }

    public void setTipoEstacao(String tipoEstacao) {
        this.tipoEstacao = tipoEstacao;
    }
}

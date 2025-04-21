package com.hidroweb.apiconsumer.repository;

import com.hidroweb.apiconsumer.entity.Estacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstacaoRepository extends JpaRepository<Estacao, Long> {

    List<Estacao> findByUfEstacao(String ufEstacao);

    List<Estacao> findByTipoEstacaoIgnoreCase(String tipoEstacao);

    List<Estacao> findByUfEstacaoAndTipoEstacao(String ufEstacao, String tipoEstacao);
}

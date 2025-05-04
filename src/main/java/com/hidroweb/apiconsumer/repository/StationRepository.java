package com.hidroweb.apiconsumer.repository;

import com.hidroweb.apiconsumer.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findByUfEstacao(String ufEstacao);
    @Query("SELECT s.codigoEstacao FROM Station s")
    Set<Long> findAllCodigosEstacao();
}

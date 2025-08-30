package com.hidroweb.apiconsumer.service;

import com.hidroweb.apiconsumer.domain.Station;
import com.hidroweb.apiconsumer.exception.ResourceNotFoundException;
import com.hidroweb.apiconsumer.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public List<Station> findByUf(String uf) {
        List<Station> stations = stationRepository.findByUfEstacao(uf.toUpperCase());
        if (stations.isEmpty()) {
            throw new ResourceNotFoundException("Stations with the UF not found");
        }
        return stations;
    }
}

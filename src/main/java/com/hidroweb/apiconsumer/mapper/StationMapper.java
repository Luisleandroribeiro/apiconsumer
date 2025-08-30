package com.hidroweb.apiconsumer.mapper;

import com.hidroweb.apiconsumer.domain.Station;
import com.hidroweb.apiconsumer.response.StationGetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface  StationMapper {

    StationGetResponse toStationGetResponse(Station station);

    List<StationGetResponse> toStationGetResponseList(List<Station> stations);
}

package com.hidroweb.apiconsumer.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class StationGetResponse {
    private Long id;
    private Long codigoEstacao;
    private String estacaoNome;
    private String tipoEstacao;

}

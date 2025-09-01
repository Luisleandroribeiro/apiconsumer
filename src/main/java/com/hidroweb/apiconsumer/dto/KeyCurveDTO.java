package com.hidroweb.apiconsumer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeyCurveDTO {
    private List<Double> coefficients;
    private double h0;
    private String equation;
    private String modelName;
    private double rSquared;
}

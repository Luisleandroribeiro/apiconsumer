package com.hidroweb.apiconsumer.regression;

import java.util.List;

public interface RegressionModel {
    RegressionResult fit(List<Double> x, List<Double> y);
    String getName(); // Ex: "Linear", "Polynomial", "Exponential"
}

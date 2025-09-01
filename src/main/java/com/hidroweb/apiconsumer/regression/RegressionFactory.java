package com.hidroweb.apiconsumer.regression;


import com.hidroweb.apiconsumer.regression.models.ExponentialRegressionModel;
import com.hidroweb.apiconsumer.regression.models.LinearRegressionModel;
import com.hidroweb.apiconsumer.regression.models.PolynomialRegressionModel;
import com.hidroweb.apiconsumer.regression.models.PowerLawRegressionModel;
import com.hidroweb.apiconsumer.regression.models.LogarithmicRegressionModel;
/**
 * Factory class for creating regression models.
 */
public class RegressionFactory {

    /**
     * Returns an instance of a regression model based on the model type.
     *
     * @param modelType the type of regression model (Linear, Polynomial, Exponential, PowerLaw, Logarithmic)
     * @return a RegressionModel instance
     * @throws IllegalArgumentException if the modelType is unknown
     */
    public static RegressionModel getRegressionModel(String modelType) {
        return switch (modelType.toLowerCase()) {
            case "linear" -> new LinearRegressionModel();
            case "polynomial" -> new PolynomialRegressionModel();
            case "exponential" -> new ExponentialRegressionModel();
            case "powerlaw" -> new PowerLawRegressionModel();
            case "logarithmic" -> new LogarithmicRegressionModel();
            default -> throw new IllegalArgumentException("Unknown regression model type: " + modelType);
        };
    }
}

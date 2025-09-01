package com.hidroweb.apiconsumer.regression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents the result of fitting a regression model.
 */

@Getter
@Setter
@AllArgsConstructor

/**
 * Class representing the result of a regression model.
 * Contains the found coefficients, the R² of the fit, and the equation as a String.
 */
public class RegressionResult {

    private final List<Double> coefficients; // Ex: [a, b] for y = a*x + b
    private final double rSquared;           // Quality of fit (0 to 1)
    private final String equation;           // Equation represented as a String
    private final String modelName;          // Model name (Linear, Polynomial, etc.)


    /**
     * Returns the coefficients as a formatted string.
     */
    public String getFormattedCoefficients() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        coefficients.forEach(c -> joiner.add(String.format("%.4f", c)));
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "Model: " + modelName + "\n" +
                "Equation: " + equation + "\n" +
                "Coefficients: " + getFormattedCoefficients() + "\n" +
                "R²: " + String.format("%.4f", rSquared);
    }
}


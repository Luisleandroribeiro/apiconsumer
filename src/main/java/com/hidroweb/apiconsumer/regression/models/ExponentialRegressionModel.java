package com.hidroweb.apiconsumer.regression.models;

import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Exponential regression model: y = a * e^(b * x)
 */
public class ExponentialRegressionModel implements RegressionModel {

    @Override
    public RegressionResult fit(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input lists must have the same non-zero length");
        }

        List<Double> logY = new ArrayList<>();
        for (double yi : y) {
            if (yi <= 0) {
                throw new IllegalArgumentException("All y values must be positive for exponential regression");
            }
            logY.add(Math.log(yi)); // linearize
        }

        // Now apply linear regression on (x, ln(y)) to get ln(y) = ln(a) + b*x
        int n = x.size();
        double sumX = 0;
        double sumLogY = 0;
        double sumXLogY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumLogY += logY.get(i);
            sumXLogY += x.get(i) * logY.get(i);
            sumX2 += x.get(i) * x.get(i);
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) {
            return new RegressionResult(List.of(0.0, 0.0), 0.0, "y = 0", getName());
        }

        double b = (n * sumXLogY - sumX * sumLogY) / denominator;
        double lnA = (sumLogY - b * sumX) / n;
        double a = Math.exp(lnA);

        // Calculate R^2
        double meanLogY = sumLogY / n;
        double ssTotal = 0;
        double ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double yiPredLog = lnA + b * x.get(i); // predicted ln(y)
            ssTotal += Math.pow(logY.get(i) - meanLogY, 2);
            ssResidual += Math.pow(logY.get(i) - yiPredLog, 2);
        }
        double rSquared = ssTotal == 0 ? 1.0 : 1 - (ssResidual / ssTotal);

        String equation = String.format("y = %.4f * e^(%.4f * x)", a, b);

        return new RegressionResult(List.of(a, b), rSquared, equation, getName());
    }

    @Override
    public String getName() {
        return "Exponential";
    }
}

package com.hidroweb.apiconsumer.regression.models;

import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;

import java.util.List;

/**
 * Linear regression model: y = a * x + b
 */
public class LinearRegressionModel implements RegressionModel {

    @Override
    public RegressionResult fit(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input lists must have the same non-zero length");
        }

        int n = x.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) {
            return new RegressionResult(List.of(0.0, 0.0), 0.0, "y = 0", getName());
        }

        double a = (n * sumXY - sumX * sumY) / denominator; // slope
        double b = (sumY - a * sumX) / n;                   // intercept

        // Calculate R^2
        double meanY = sumY / n;
        double ssTotal = 0;
        double ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double yi = y.get(i);
            double yiPred = a * x.get(i) + b;
            ssTotal += Math.pow(yi - meanY, 2);
            ssResidual += Math.pow(yi - yiPred, 2);
        }
        double rSquared = ssTotal == 0 ? 1.0 : 1 - (ssResidual / ssTotal);

        String equation = String.format("y = %.4f * x + %.4f", a, b);

        return new RegressionResult(List.of(a, b), rSquared, equation, getName());
    }

    @Override
    public String getName() {
        return "Linear";
    }
}

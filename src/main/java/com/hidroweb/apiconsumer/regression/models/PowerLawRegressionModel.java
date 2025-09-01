package com.hidroweb.apiconsumer.regression.models;

import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Power-law regression model: y = a * x^b
 */
public class PowerLawRegressionModel implements RegressionModel {

    @Override
    public RegressionResult fit(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input lists must have the same non-zero length");
        }

        List<Double> logX = new ArrayList<>();
        List<Double> logY = new ArrayList<>();

        for (int i = 0; i < x.size(); i++) {
            double xi = x.get(i);
            double yi = y.get(i);

            if (xi <= 0 || yi <= 0) {
                throw new IllegalArgumentException("All x and y values must be positive for power-law regression");
            }

            logX.add(Math.log10(xi));
            logY.add(Math.log10(yi));
        }

        // Apply linear regression on log-log: log(y) = log(a) + b*log(x)
        int n = logX.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double lx = logX.get(i);
            double ly = logY.get(i);
            sumX += lx;
            sumY += ly;
            sumXY += lx * ly;
            sumX2 += lx * lx;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) {
            return new RegressionResult(List.of(0.0, 0.0), 0.0, "y = 0", getName());
        }

        double b = (n * sumXY - sumX * sumY) / denominator;
        double logA = (sumY - b * sumX) / n;
        double a = Math.pow(10, logA);

        // Compute R^2
        double meanY = sumY / n;
        double ssTotal = 0;
        double ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double yiPredLog = logA + b * logX.get(i);
            ssTotal += Math.pow(logY.get(i) - meanY, 2);
            ssResidual += Math.pow(logY.get(i) - yiPredLog, 2);
        }
        double rSquared = ssTotal == 0 ? 1.0 : 1 - (ssResidual / ssTotal);

        String equation = String.format("y = %.4f * x^%.4f", a, b);

        return new RegressionResult(List.of(a, b), rSquared, equation, getName());
    }

    @Override
    public String getName() {
        return "PowerLaw";
    }
}

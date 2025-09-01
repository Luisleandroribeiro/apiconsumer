package com.hidroweb.apiconsumer.regression.models;

import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Logarithmic regression model: y = a + b * ln(x)
 */
public class LogarithmicRegressionModel implements RegressionModel {

    @Override
    public RegressionResult fit(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input lists must have the same non-zero length");
        }

        // Transform x to ln(x)
        List<Double> lnX = new ArrayList<>();
        for (double xi : x) {
            if (xi <= 0) {
                throw new IllegalArgumentException("All x values must be positive for logarithmic regression");
            }
            lnX.add(Math.log(xi));
        }

        int n = lnX.size();
        double sumLnX = 0;
        double sumY = 0;
        double sumLnXY = 0;
        double sumLnX2 = 0;

        for (int i = 0; i < n; i++) {
            sumLnX += lnX.get(i);
            sumY += y.get(i);
            sumLnXY += lnX.get(i) * y.get(i);
            sumLnX2 += lnX.get(i) * lnX.get(i);
        }

        double denominator = n * sumLnX2 - sumLnX * sumLnX;
        if (denominator == 0) {
            return new RegressionResult(List.of(0.0, 0.0), 0.0, "y = 0", getName());
        }

        double b = (n * sumLnXY - sumLnX * sumY) / denominator;
        double a = (sumY - b * sumLnX) / n;

        // Calculate R^2
        double meanY = sumY / n;
        double ssTotal = 0;
        double ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double yiPred = a + b * lnX.get(i);
            ssTotal += Math.pow(y.get(i) - meanY, 2);
            ssResidual += Math.pow(y.get(i) - yiPred, 2);
        }
        double rSquared = ssTotal == 0 ? 1.0 : 1 - (ssResidual / ssTotal);

        String equation = String.format("y = %.4f + %.4f * ln(x)", a, b);

        return new RegressionResult(List.of(a, b), rSquared, equation, getName());
    }

    @Override
    public String getName() {
        return "Logarithmic";
    }
}

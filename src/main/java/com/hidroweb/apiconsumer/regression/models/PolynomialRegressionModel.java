package com.hidroweb.apiconsumer.regression.models;

import com.hidroweb.apiconsumer.regression.RegressionModel;
import com.hidroweb.apiconsumer.regression.RegressionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Polynomial regression model: y = a0 + a1*x + a2*x^2 + ... + an*x^n
 */
public class PolynomialRegressionModel implements RegressionModel {

    private final int degree;

    public PolynomialRegressionModel() {
        this.degree = 2; // Default to quadratic; you can change as needed
    }

    public PolynomialRegressionModel(int degree) {
        if (degree < 1) {
            throw new IllegalArgumentException("Polynomial degree must be >= 1");
        }
        this.degree = degree;
    }

    @Override
    public RegressionResult fit(List<Double> x, List<Double> y) {
        if (x.size() != y.size() || x.isEmpty()) {
            throw new IllegalArgumentException("Input lists must have the same non-zero length");
        }

        int n = x.size();
        int m = degree + 1; // number of coefficients

        // Create Vandermonde matrix
        double[][] X = new double[n][m];
        for (int i = 0; i < n; i++) {
            double xi = 1.0;
            for (int j = 0; j < m; j++) {
                X[i][j] = xi;
                xi *= x.get(i);
            }
        }

        // Solve normal equations (X^T * X) * a = X^T * y
        double[][] XtX = new double[m][m];
        double[] Xty = new double[m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Xty[j] += X[i][j] * y.get(i);
                for (int k = 0; k < m; k++) {
                    XtX[j][k] += X[i][j] * X[i][k];
                }
            }
        }

        // Solve linear system using Gaussian elimination
        double[] coefficients = gaussianElimination(XtX, Xty);

        // Compute R^2
        double meanY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double ssTotal = 0;
        double ssResidual = 0;
        for (int i = 0; i < n; i++) {
            double yiPred = 0;
            double xiPower = 1;
            for (double coeff : coefficients) {
                yiPred += coeff * xiPower;
                xiPower *= x.get(i);
            }
            ssTotal += Math.pow(y.get(i) - meanY, 2);
            ssResidual += Math.pow(y.get(i) - yiPred, 2);
        }
        double rSquared = ssTotal == 0 ? 1.0 : 1 - (ssResidual / ssTotal);

        // Create equation string
        StringBuilder equation = new StringBuilder("y = ");
        for (int i = 0; i < coefficients.length; i++) {
            if (i > 0) {
                equation.append(" + ");
            }
            equation.append(String.format("%.4f", coefficients[i]));
            if (i >= 1) {
                equation.append("*x");
                if (i > 1) {
                    equation.append("^").append(i);
                }
            }
        }

        return new RegressionResult(toObject(coefficients), rSquared, equation.toString(), getName());
    }

    @Override
    public String getName() {
        return "Polynomial (degree " + degree + ")";
    }

    // Gaussian elimination solver for linear system
    private double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;
        for (int p = 0; p < n; p++) {
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            double[] temp = A[p];
            A[p] = A[max];
            A[max] = temp;
            double t = b[p];
            b[p] = b[max];
            b[max] = t;

            for (int i = p + 1; i < n; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < n; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }
        return x;
    }

    private List<Double> toObject(double[] arr) {
        List<Double> list = new ArrayList<>();
        for (double v : arr) {
            list.add(v);
        }
        return list;
    }
}

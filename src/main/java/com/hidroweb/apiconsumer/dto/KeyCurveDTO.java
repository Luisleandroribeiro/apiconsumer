package com.hidroweb.apiconsumer.dto;

public class KeyCurveDTO {
    private double a;
    private double b;
    private double h0;

    // adicione o campo equation:
    private String equation;

    // Construtor
    public KeyCurveDTO(double a, double b, double h0) {
        this.a = a;
        this.b = b;
        this.h0 = h0;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getH0() {
        return h0;
    }

    public void setH0(double h0) {
        this.h0 = h0;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }
}

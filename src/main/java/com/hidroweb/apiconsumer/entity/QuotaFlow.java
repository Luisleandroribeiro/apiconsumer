package com.hidroweb.apiconsumer.entity;

public class QuotaFlow {

    public double quota;
    public double flow;

    public QuotaFlow (double quota, double flow){
        this.quota=quota;
        this.flow=flow;
    }

    public double getQuota() {
        return quota;
    }

    public void setQuota(double quota) {
        this.quota = quota;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(double flow) {
        this.flow = flow;
    }
}

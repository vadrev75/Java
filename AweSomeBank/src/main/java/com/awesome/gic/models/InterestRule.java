package com.awesome.gic.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InterestRule implements Comparable<InterestRule> {
    private LocalDate date;
    private String ruleId;
    private double rate;

    public InterestRule(LocalDate date, String ruleId, double rate) {
        this.date = date;
        this.ruleId = ruleId;
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getRuleId() {
        return ruleId;
    }

    public double getRate() {
        return rate;
    }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Override
    public int compareTo(InterestRule other) {
        return this.date.compareTo(other.date);
    }
}

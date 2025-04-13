package com.awesome.gic.interfaces;

import com.awesome.gic.models.InterestRule;

import java.time.LocalDate;
import java.util.List;

public interface InterestRuleService {
    void addInterestRule(String dateStr, String ruleId, double rate) throws Exception;
    List<InterestRule> getAllInterestRules();
    InterestRule getApplicableInterestRule(LocalDate date);
}
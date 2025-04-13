package com.awesome.gic.services;

import com.awesome.gic.interfaces.InterestRuleService;
import com.awesome.gic.models.InterestRule;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterestRuleServiceImpl implements InterestRuleService {
    private List<InterestRule> interestRules;

    public InterestRuleServiceImpl() {
        interestRules = new ArrayList<>();
    }

    @Override
    public void addInterestRule(String dateStr, String ruleId, double rate) throws Exception {
        if (rate <= 0 || rate >= 100) {
            throw new Exception("Interest rate should be greater than 0 and less than 100");
        }

        LocalDate date = parseDate(dateStr);

        // Remove existing rule on the same date if any
        interestRules.removeIf(rule -> rule.getDate().equals(date));

        // Add new rule
        InterestRule rule = new InterestRule(date, ruleId, rate);
        interestRules.add(rule);

        // Sort by date
        Collections.sort(interestRules);
    }

    @Override
    public List<InterestRule> getAllInterestRules() {
        return interestRules;
    }

    @Override
    public InterestRule getApplicableInterestRule(LocalDate date) {
        InterestRule applicableRule = null;

        for (InterestRule rule : interestRules) {
            if (!rule.getDate().isAfter(date)) {
                applicableRule = rule;
            } else {
                break;
            }
        }

        return applicableRule;
    }

    private LocalDate parseDate(String dateStr) throws Exception {
        if (!dateStr.matches("\\d{8}")) {
            throw new Exception("Date should be in YYYYMMdd format");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new Exception("Invalid date format. Please use YYYYMMdd");
        }
    }
}

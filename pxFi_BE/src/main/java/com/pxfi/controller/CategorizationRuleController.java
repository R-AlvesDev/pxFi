package com.pxfi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pxfi.model.CategorizationRule;
import com.pxfi.model.TestRuleRequest;
import com.pxfi.model.TestRuleResponse;
import com.pxfi.model.Transaction;
import com.pxfi.service.CategorizationRuleService;
import com.pxfi.service.RuleEngineService;

@RestController
@RequestMapping("/api/rules")
public class CategorizationRuleController {

    private final CategorizationRuleService ruleService;
    private final RuleEngineService ruleEngineService;

    public CategorizationRuleController(CategorizationRuleService ruleService, RuleEngineService ruleEngineService) {
        this.ruleService = ruleService;
        this.ruleEngineService = ruleEngineService; 
    }

    @GetMapping
    public ResponseEntity<List<CategorizationRule>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @PostMapping
    public ResponseEntity<CategorizationRule> createRule(@RequestBody CategorizationRule rule) {
        return ResponseEntity.ok(ruleService.createRule(rule));
    }

    @PostMapping("/apply-all")
    public ResponseEntity<Map<String, Long>> applyAllRules() {
        long count = ruleService.applyAllRules();
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<TestRuleResponse> testRule(@RequestBody TestRuleRequest request) {
        TestRuleResponse response = ruleService.testRule(request);
        return ResponseEntity.ok(response);
    }
    
}
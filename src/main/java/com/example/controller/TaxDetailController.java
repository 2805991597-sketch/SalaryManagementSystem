package com.example.controller;

import com.example.pojo.TaxDetail;
import com.example.service.TaxDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tax-detail")
@RequiredArgsConstructor
public class TaxDetailController {
    private final TaxDetailService taxDetailService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) String taxType) {
        Map<String, Object> result = taxDetailService.list(taxType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculate(
            @RequestParam java.math.BigDecimal monthlyIncome,
            @RequestParam(required = false) java.math.BigDecimal annualBonus) {
        Map<String, Object> result = taxDetailService.calculatePersonalTax(monthlyIncome, annualBonus);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody TaxDetail taxDetail) {
        Map<String, Object> response = new HashMap<>();
        taxDetailService.save(taxDetail);
        response.put("code", 200);
        response.put("msg", "保存成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam Integer id) {
        Map<String, Object> response = new HashMap<>();
        taxDetailService.delete(id);
        response.put("code", 200);
        response.put("msg", "删除成功");
        return ResponseEntity.ok(response);
    }
}
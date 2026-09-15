package com.example.controller;

import com.example.pojo.YearEndBonus;
import com.example.service.YearEndBonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/year-end-bonus")
@RequiredArgsConstructor
public class YearEndBonusController {
    private final YearEndBonusService yearEndBonusService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Integer empId,
            @RequestParam(required = false) String name) {
        Integer yearInt = null;
        if (year != null && !year.trim().isEmpty()) {
            try {
                yearInt = Integer.parseInt(year.trim());
            } catch (NumberFormatException e) {
                // ignore invalid year format
            }
        }
        Map<String, Object> result = yearEndBonusService.list(yearInt, empId, name);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/annual-summary")
    public ResponseEntity<Map<String, Object>> getAnnualSalarySummary(
            @RequestParam Integer empId,
            @RequestParam Integer year) {
        Map<String, Object> result = yearEndBonusService.getAnnualSalarySummary(empId, year);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody YearEndBonus bonus) {
        Map<String, Object> response = new HashMap<>();
        try {
            yearEndBonusService.save(bonus);
            response.put("code", 200);
            response.put("msg", "保存成功");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("code", 400);
            response.put("msg", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam Integer id) {
        Map<String, Object> response = new HashMap<>();
        yearEndBonusService.delete(id);
        response.put("code", 200);
        response.put("msg", "删除成功");
        return ResponseEntity.ok(response);
    }
}
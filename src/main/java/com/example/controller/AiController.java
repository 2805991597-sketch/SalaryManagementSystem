package com.example.controller;

import com.example.mapper.SalaryMapper;
import com.example.mapper.TaxDetailMapper;
import com.example.pojo.Salary;
import com.example.pojo.SalaryVO;
import com.example.pojo.TaxDetail;
import com.example.service.SparkAiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salary")
@Slf4j
public class AiController {

    @Resource
    private SparkAiService sparkAiService;

    @Resource
    private SalaryMapper salaryMapper;

    @Resource
    private TaxDetailMapper taxDetailMapper;

    private double calculateMonthlyTax(double taxableIncome) {
        if (taxableIncome <= 0) return 0;
        
        List<TaxDetail> taxRates = taxDetailMapper.list("个人所得税", true);
        for (TaxDetail rate : taxRates) {
            double min = rate.getMinThreshold() != null ? rate.getMinThreshold().doubleValue() : 0;
            Double max = rate.getMaxLimit() != null ? rate.getMaxLimit().doubleValue() : null;
            double taxRate = rate.getRate().doubleValue();
            double quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction().doubleValue() : 0;
            
            if (max != null) {
                if (taxableIncome > min && taxableIncome <= max) {
                    return Math.round((taxableIncome * taxRate - quickDeduction) * 100) / 100.0;
                }
            } else {
                if (taxableIncome > min) {
                    return Math.round((taxableIncome * taxRate - quickDeduction) * 100) / 100.0;
                }
            }
        }
        
        return 0;
    }

    private double calculateBonusTax(double bonus) {
        if (bonus <= 0) return 0;
        double monthly = bonus / 12;
        
        List<TaxDetail> bonusRates = taxDetailMapper.list("年终奖单独计税税率", true);
        for (TaxDetail rate : bonusRates) {
            double min = rate.getMinThreshold() != null ? rate.getMinThreshold().doubleValue() : 0;
            Double max = rate.getMaxLimit() != null ? rate.getMaxLimit().doubleValue() : null;
            double taxRate = rate.getRate().doubleValue();
            double quickDeduction = rate.getQuickDeduction() != null ? rate.getQuickDeduction().doubleValue() : 0;
            
            if (max != null) {
                if (monthly > min && monthly <= max) {
                    return Math.round((bonus * taxRate - quickDeduction) * 100) / 100.0;
                }
            } else {
                if (monthly > min) {
                    return Math.round((bonus * taxRate - quickDeduction) * 100) / 100.0;
                }
            }
        }
        
        return 0;
    }
    
    private double findOptimalBonus(double totalIncome, double totalSubsidies) {
        double bestBonus = 0;
        double bestTax = Double.MAX_VALUE;
        
        double[] criticalPoints = {0, 36000, 144000, 300000, 420000, 660000, 960000};
        
        for (double cp : criticalPoints) {
            if (cp > totalIncome) continue;
            
            for (int delta = -500; delta <= 500; delta += 100) {
                double bonus = cp + delta;
                if (bonus < 0 || bonus > totalIncome) continue;
                
                double basic = (totalIncome - bonus) / 12 - totalSubsidies;
                if (basic < 0) continue;
                
                double monthlyIncome = basic + totalSubsidies;
                double taxable = monthlyIncome - 5000;
                double monthlyTax = calculateMonthlyTax(taxable);
                double annualSalaryTax = monthlyTax * 12;
                double bonusTax = calculateBonusTax(bonus);
                double totalTax = annualSalaryTax + bonusTax;
                
                if (totalTax < bestTax) {
                    bestTax = totalTax;
                    bestBonus = bonus;
                }
            }
        }
        
        if (bestBonus == 0) {
            for (double bonus = 0; bonus <= totalIncome; bonus += 5000) {
                double basic = (totalIncome - bonus) / 12 - totalSubsidies;
                if (basic < 0) continue;
                
                double monthlyIncome = basic + totalSubsidies;
                double taxable = monthlyIncome - 5000;
                double monthlyTax = calculateMonthlyTax(taxable);
                double annualSalaryTax = monthlyTax * 12;
                double bonusTax = calculateBonusTax(bonus);
                double totalTax = annualSalaryTax + bonusTax;
                
                if (totalTax < bestTax) {
                    bestTax = totalTax;
                    bestBonus = bonus;
                }
            }
        }
        
        return Math.round(bestBonus);
    }

    @PostMapping("/aiPlanning")
    public ResponseEntity<Map<String, Object>> aiPlanning(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer empId = (Integer) request.get("empId");
            Double targetSalary = request.get("targetSalary") != null ? ((Number) request.get("targetSalary")).doubleValue() : 0.0;
            String objective = (String) request.get("objective");
            if (objective == null) objective = "tax_optimization";
            double yearEndBonus = request.containsKey("yearEndBonus") && request.get("yearEndBonus") != null
                    ? ((Number) request.get("yearEndBonus")).doubleValue() : 0.0;
            Double basicSalaryParam = request.containsKey("basicSalary") && request.get("basicSalary") != null
                    ? ((Number) request.get("basicSalary")).doubleValue() : null;
            Integer year = null;
            if (request.containsKey("year") && request.get("year") != null) {
                Object yearObj = request.get("year");
                if (yearObj instanceof Integer) {
                    year = (Integer) yearObj;
                } else if (yearObj instanceof String) {
                    try {
                        year = Integer.parseInt((String) yearObj);
                    } catch (NumberFormatException e) {
                        log.warn("年份格式错误: {}", yearObj);
                    }
                }
            }

            List<SalaryVO> salaryList = salaryMapper.list(null, null, null, null, empId, "DESC");
            Map<String, Object> salaryData = new HashMap<>();
            double currentBasic = 0;
            double foodSubsidy = 0;
            double trafficSubsidy = 0;
            double housingSubsidy = 0;
            double levelSubsidy = 0;
            double currentMonthlyIncome;
            
            // 如果前端传递了basicSalary（平均工资），直接使用
            if (basicSalaryParam != null && basicSalaryParam > 0) {
                currentBasic = basicSalaryParam;
                // 查询一条工资记录获取补贴信息
                if (salaryList != null && !salaryList.isEmpty()) {
                    Salary s = salaryList.get(0);
                    foodSubsidy = s.getFoodSubsidy() != null ? s.getFoodSubsidy().doubleValue() : 0;
                    trafficSubsidy = s.getTrafficSubsidy() != null ? s.getTrafficSubsidy().doubleValue() : 0;
                    housingSubsidy = s.getHousingSubsidy() != null ? s.getHousingSubsidy().doubleValue() : 0;
                    levelSubsidy = s.getLevelSubsidy() != null ? s.getLevelSubsidy().doubleValue() : 0;
                    salaryData.put("basicSalary", currentBasic);
                    salaryData.put("foodSubsidy", foodSubsidy);
                    salaryData.put("trafficSubsidy", trafficSubsidy);
                    salaryData.put("housingSubsidy", housingSubsidy);
                    salaryData.put("levelSubsidy", levelSubsidy);
                }
            } else if (salaryList != null && !salaryList.isEmpty()) {
                Salary s = null;
                if (year != null) {
                    for (Salary salary : salaryList) {
                        if (salary.getMonth() != null && salary.getMonth().getYear() == year) {
                            s = salary;
                            break;
                        }
                    }
                }
                if (s == null) {
                    s = salaryList.get(0);
                }
                
                salaryData.put("id", s.getId());
                currentBasic = s.getBasicSalary() != null ? s.getBasicSalary().doubleValue() : 0;
                foodSubsidy = s.getFoodSubsidy() != null ? s.getFoodSubsidy().doubleValue() : 0;
                trafficSubsidy = s.getTrafficSubsidy() != null ? s.getTrafficSubsidy().doubleValue() : 0;
                housingSubsidy = s.getHousingSubsidy() != null ? s.getHousingSubsidy().doubleValue() : 0;
                levelSubsidy = s.getLevelSubsidy() != null ? s.getLevelSubsidy().doubleValue() : 0;
                
                salaryData.put("basicSalary", currentBasic);
                salaryData.put("foodSubsidy", foodSubsidy);
                salaryData.put("trafficSubsidy", trafficSubsidy);
                salaryData.put("housingSubsidy", housingSubsidy);
                salaryData.put("levelSubsidy", levelSubsidy);
                if (s.getMonth() != null) {
                    salaryData.put("month", s.getMonth().toString().substring(0, 7));
                }
            }
            
            // 计算月度应发工资
            BigDecimal totalIncome = BigDecimal.valueOf(currentBasic);
            totalIncome = totalIncome.add(BigDecimal.valueOf(foodSubsidy));
            totalIncome = totalIncome.add(BigDecimal.valueOf(trafficSubsidy));
            totalIncome = totalIncome.add(BigDecimal.valueOf(housingSubsidy));
            totalIncome = totalIncome.add(BigDecimal.valueOf(levelSubsidy));
            currentMonthlyIncome = totalIncome.doubleValue();
            double currentTaxableIncome = currentMonthlyIncome - 5000;
            double currentMonthlyTax = calculateMonthlyTax(currentTaxableIncome);
            double currentAnnualSalaryTax = currentMonthlyTax * 12;
            double currentBonusTax = calculateBonusTax(yearEndBonus);
            double currentTotalTax = currentAnnualSalaryTax + currentBonusTax;

            String aiResult = sparkAiService.generateSalaryPlan(empId, targetSalary, objective, yearEndBonus);

            Map<String, Object> suggestedData = new HashMap<>();
            List<String> suggestions = new ArrayList<>();
            
            double currentAnnualIncome = currentMonthlyIncome * 12 + yearEndBonus;
            
            double suggestedBasic = currentBasic;
            double suggestedBonus = yearEndBonus;
            
            int jsonStart = aiResult.indexOf("{");
            int jsonEnd = aiResult.lastIndexOf("}");
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonStr = aiResult.substring(jsonStart, jsonEnd + 1);
                jsonStr = jsonStr.replaceAll("\\n", " ").replaceAll("\\r", " ");
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> jsonData = mapper.readValue(jsonStr, new com.fasterxml.jackson.core.type.TypeReference<>() {
                    });
                    
                    if (jsonData.containsKey("suggestedBasicSalary")) {
                        Object bs = jsonData.get("suggestedBasicSalary");
                        if (bs instanceof Number) {
                            suggestedBasic = ((Number) bs).doubleValue();
                        }
                    }
                    if (jsonData.containsKey("suggestedBonus")) {
                        Object bonus = jsonData.get("suggestedBonus");
                        if (bonus instanceof Number) {
                            suggestedBonus = ((Number) bonus).doubleValue();
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析AI返回的JSON失败: {}", e.getMessage());
                }
            }
            
            double totalSubsidies = foodSubsidy + trafficSubsidy + housingSubsidy + levelSubsidy;
            double suggestedAnnualIncome = (suggestedBasic + totalSubsidies) * 12 + suggestedBonus;
            if (Math.abs(suggestedAnnualIncome - currentAnnualIncome) > 100) {
                log.warn("AI建议的年度总收入({})与当前年度总收入({})不一致，已自动修正", suggestedAnnualIncome, currentAnnualIncome);
                suggestedBasic = Math.round((currentAnnualIncome - suggestedBonus) / 12 - totalSubsidies);
                suggestedBonus = Math.round(currentAnnualIncome - (suggestedBasic + totalSubsidies) * 12);
            }
            
            double suggestedMonthlyIncome = suggestedBasic + totalSubsidies;
            double suggestedTaxableIncome = suggestedMonthlyIncome - 5000;
            double suggestedMonthlyTax = calculateMonthlyTax(suggestedTaxableIncome);
            double suggestedAnnualSalaryTax = suggestedMonthlyTax * 12;
            double suggestedBonusTax = calculateBonusTax(suggestedBonus);
            double suggestedTotalTax = suggestedAnnualSalaryTax + suggestedBonusTax;
            
            log.info("使用系统步长算法寻找最优税负方案");
            double adjustedIncome = Math.round(currentAnnualIncome);
            
            double optimalBonus = findOptimalBonus(adjustedIncome, totalSubsidies);
            double optimalBasic = (adjustedIncome - optimalBonus) / 12 - totalSubsidies;
            
            double optimalMonthlyIncome = optimalBasic + totalSubsidies;
            double optimalTaxable = optimalMonthlyIncome - 5000;
            double optimalMonthlyTax = calculateMonthlyTax(optimalTaxable);
            double optimalAnnualSalaryTax = optimalMonthlyTax * 12;
            double optimalBonusTax = calculateBonusTax(optimalBonus);
            double optimalTotalTax = optimalAnnualSalaryTax + optimalBonusTax;
            
            if (optimalTotalTax < currentTotalTax - 1) {
                suggestedBasic = optimalBasic;
                suggestedBonus = optimalBonus;
                suggestedMonthlyIncome = optimalMonthlyIncome;
                suggestedMonthlyTax = optimalMonthlyTax;
                suggestedAnnualSalaryTax = optimalAnnualSalaryTax;
                suggestedBonusTax = optimalBonusTax;
                suggestedTotalTax = optimalTotalTax;
                
                log.info("步长算法找到更优方案: 税负从 {} 降至 {}", currentTotalTax, suggestedTotalTax);
            }
            
            double taxSavings = currentTotalTax - suggestedTotalTax;
            if (taxSavings < 0) {
                log.warn("建议方案税负更高({} -> {}), 保持原方案", currentTotalTax, suggestedTotalTax);
                suggestedBasic = currentBasic;
                suggestedBonus = yearEndBonus;
                suggestedMonthlyIncome = currentMonthlyIncome;
                suggestedMonthlyTax = currentMonthlyTax;
                suggestedAnnualSalaryTax = currentAnnualSalaryTax;
                suggestedBonusTax = currentBonusTax;
                suggestedTotalTax = currentTotalTax;
                taxSavings = 0;
            }
            
            suggestedData.put("suggestedBasicSalary", suggestedBasic);
            suggestedData.put("suggestedBonus", suggestedBonus);
            
            if (Math.abs(suggestedBasic - currentBasic) > 100) {
                suggestions.add(String.format("将基本工资从%.2f元调整到%.2f元", currentBasic, suggestedBasic));
            }
            if (Math.abs(suggestedBonus - yearEndBonus) > 100) {
                suggestions.add(String.format("将年终奖从%.2f元调整到%.2f元", yearEndBonus, suggestedBonus));
            }
            // 如果基本工资和年终奖变化不大，但税负有显著节省，也要给出提示
            if (taxSavings > 100 && suggestions.isEmpty()) {
                suggestions.add(String.format("通过微调工资结构，预计年度税负可减少%.2f元", taxSavings));
            }
            if (suggestions.isEmpty()) {
                suggestions.add("当前工资分配已为最优方案，无需调整");
            }
            
            // 检查是否真的有变化：基本工资/年终奖变化超过100元，或税负节省超过100元
            boolean hasBasicChange = Math.abs(suggestedBasic - currentBasic) > 100;
            boolean hasBonusChange = Math.abs(suggestedBonus - yearEndBonus) > 100;
            boolean hasTaxSavings = taxSavings > 100;
            boolean hasChange = hasBasicChange || hasBonusChange || hasTaxSavings;
            
            if (hasChange) {
                log.info("调用AI分析最终确定的方案 - 基本工资变化:{}, 年终奖变化:{}, 税负节省:{}", 
                         hasBasicChange, hasBonusChange, hasTaxSavings);
                aiResult = sparkAiService.analyzeSalaryPlan(empId, currentBasic, yearEndBonus, 
                                                             suggestedBasic, suggestedBonus, 
                                                             currentTotalTax, suggestedTotalTax);
            } else {
                // 没有变化时，直接返回简单分析
                aiResult = "当前工资分配已为最优方案，现有薪资结构在年度总收入不变的前提下，通过合理的基本工资与年终奖比例配置，已实现税负最优化。该方案充分利用了个人所得税税率临界点的优势，确保员工税后收入最大化。建议继续保持当前的薪资分配方式，同时可关注后续税收政策变化，适时进行相应调整。";
            }
            
            log.info("AI分析参数 - 当前: 基本工资={}, 年终奖={}, 税负={}", currentBasic, yearEndBonus, currentTotalTax);
            log.info("AI分析参数 - 建议: 基本工资={}, 年终奖={}, 税负={}", suggestedBasic, suggestedBonus, suggestedTotalTax);
            
            log.info("税负对比 - 当前: {}, 建议: {}, 节省: {}", currentTotalTax, suggestedTotalTax, taxSavings);

            response.put("code", 200);
            response.put("salaryData", salaryData);
            response.put("suggestedData", suggestedData);
            response.put("analysis", aiResult);
            response.put("suggestions", suggestions);
            
            Map<String, Object> taxData = new HashMap<>();
            taxData.put("currentMonthlyIncome", currentMonthlyIncome);
            taxData.put("currentMonthlyTax", currentMonthlyTax);
            taxData.put("currentAnnualSalaryTax", currentAnnualSalaryTax);
            taxData.put("currentBonusTax", currentBonusTax);
            taxData.put("currentTotalTax", currentTotalTax);
            taxData.put("suggestedMonthlyIncome", suggestedMonthlyIncome);
            taxData.put("suggestedMonthlyTax", suggestedMonthlyTax);
            taxData.put("suggestedAnnualSalaryTax", suggestedAnnualSalaryTax);
            taxData.put("suggestedBonusTax", suggestedBonusTax);
            taxData.put("suggestedTotalTax", suggestedTotalTax);
            taxData.put("taxSavings", Math.max(0, taxSavings));
            response.put("taxData", taxData);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "AI筹划失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
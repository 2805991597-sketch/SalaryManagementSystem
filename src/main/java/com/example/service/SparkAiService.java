package com.example.service;

import com.example.config.SparkAiConfig;
import com.example.mapper.EmployeeMapper;
import com.example.mapper.SalaryMapper;
import com.example.pojo.Employee;
import com.example.pojo.Salary;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class SparkAiService {

    private static final String LOG_API_CALL = "调用讯飞AI HTTP API，model: {}";
    private static final String LOG_API_RESPONSE = "讯飞AI响应: {}";
    private static final String LOG_API_ERROR = "讯飞API返回错误: {}";
    private static final String LOG_API_ERROR_CODE = "讯飞API返回错误码: {}, 信息: {}";
    private static final String LOG_API_EXCEPTION = "讯飞API调用异常: {}";

    @Resource
    private SparkAiConfig sparkAiConfig;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private SalaryMapper salaryMapper;

    @Resource
    private RestTemplate restTemplate;

    public String generateSalaryPlan(Integer empId, Double targetSalary, String objective) {
        return generateSalaryPlan(empId, targetSalary, objective, 0.0);
    }

    public String generateSalaryPlan(Integer empId, Double targetSalary, String objective, Double yearEndBonus) {
        try {
            Employee employee = getEmployeeById(empId);
            List<Salary> salaryList = salaryMapper.list(null, null, null, null, empId, "DESC");
            Salary latestSalary = null;
            if (salaryList != null && !salaryList.isEmpty()) {
                latestSalary = salaryList.get(0);
            }

            String objectiveDesc = "税负优化（合理避税）";
            if ("income_max".equals(objective)) {
                objectiveDesc = "税后收入最大化";
            } else if ("balance".equals(objective)) {
                objectiveDesc = "均衡分配（月度平滑）";
            }

            String prompt = buildPrompt(employee, latestSalary, targetSalary, objectiveDesc, yearEndBonus);
            return callSparkApi(prompt);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error(LOG_API_EXCEPTION, e.getMessage(), e);
            return "AI服务暂时不可用，请稍后重试。错误信息: " + e.getMessage();
        }
    }

    public String analyzeSalaryPlan(Integer empId, Double currentBasic, Double currentBonus,
                                    Double suggestedBasic, Double suggestedBonus,
                                    Double currentTax, Double suggestedTax) {
        try {
            Employee employee = getEmployeeById(empId);
            double taxSavings = currentTax - suggestedTax;

            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一位专业的税务筹划顾问，请基于以下已有的数据进行分析。\n\n");

            prompt.append("【绝对禁止】\n");
            prompt.append("🚫 绝对禁止进行任何计算！所有数据均已由专业算法精确计算完成。\n");
            prompt.append("🚫 绝对不要重新计算、验证或质疑任何数据。\n");
            prompt.append("🚫 不要计算总收入、不要计算税率、不要计算任何数值。\n");
            prompt.append("🚫 绝对不要使用任何标题格式，包括 \"###\"、数字序号、冒号标题等。\n");
            prompt.append("🚫 不要使用 \"方案评价\"、\"优化原理\"、\"注意事项\"、\"优化建议\" 等固定标题。\n");
            prompt.append("🚫 你的唯一任务是：基于给定数据进行专业分析和解读，用自然的段落形式输出。\n\n");

            prompt.append("【员工信息】\n");
            prompt.append("- 姓名：").append(nullToEmpty(employee.getName())).append("\n");
            prompt.append("- 部门：").append(nullToEmpty(employee.getDeptName())).append("\n");
            prompt.append("- 岗位：").append(nullToEmpty(employee.getPostName())).append("\n\n");

            prompt.append("【当前方案数据 - 直接使用，不要计算】\n");
            prompt.append("- 基本工资：").append(String.format("%.2f", currentBasic)).append("元\n");
            prompt.append("- 年终奖：").append(String.format("%.2f", currentBonus)).append("元\n");
            prompt.append("- 年度税负：").append(String.format("%.2f", currentTax)).append("元\n\n");

            prompt.append("【优化方案数据 - 直接使用，不要计算】\n");
            prompt.append("- 基本工资：").append(String.format("%.2f", suggestedBasic)).append("元\n");
            prompt.append("- 年终奖：").append(String.format("%.2f", suggestedBonus)).append("元\n");
            prompt.append("- 年度税负：").append(String.format("%.2f", suggestedTax)).append("元\n\n");

            if (taxSavings > 0) {
                prompt.append("【优化效果数据 - 直接使用，不要计算】\n");
                prompt.append("- 预计节省税额：").append(String.format("%.2f", taxSavings)).append("元\n\n");
            } else if (taxSavings < 0) {
                prompt.append("【优化效果数据 - 直接使用，不要计算】\n");
                prompt.append("- 预计增加税额：").append(String.format("%.2f", Math.abs(taxSavings))).append("元\n\n");
            }

            prompt.append("【分析要点】\n");
            prompt.append("请围绕以下要点进行分析，但不要使用任何标题：\n");
            prompt.append("- 评价优化方案的合理性和有效性\n");
            prompt.append("- 解释优化的原理和依据，特别是个税税率临界点的应用\n");
            prompt.append("- 说明实施这个方案需要注意的事项\n");
            prompt.append("- 给出其他可能的优化方向建议\n\n");

            prompt.append("【输出要求】\n");
            prompt.append("- 用简洁专业的中文进行分析\n");
            prompt.append("- 以自然流畅的段落形式输出，不要分段太多\n");
            prompt.append("- 直接开始分析内容，不要任何开场白\n");
            prompt.append("- 绝对不要使用任何标题、序号、列表\n");
            prompt.append("- 最后强调：绝对不要进行任何计算！\n");

            return callSparkApi(prompt.toString());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            log.error(LOG_API_EXCEPTION, e.getMessage(), e);
            return "AI服务暂时不可用，请稍后重试。";
        }
    }

    private String buildPrompt(Employee employee, Salary salary, Double targetSalary, String objective, Double yearEndBonus) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的税务筹划顾问，请用友好自然的语言分析以下员工的工资和年终奖分配是否合理，并给出优化建议。\n");
        if (targetSalary != null) {
            prompt.append("用户目标年薪：").append(String.format("%.2f", targetSalary)).append("元\n");
        }
        prompt.append("优化目标类型：").append(objective).append("\n");
        prompt.append("核心目标：在【年度总收入不变】的前提下，通过调整基本工资与年终奖的分配比例，降低整体税负。\n\n");

        prompt.append("【员工信息】\n");
        prompt.append("- 姓名：").append(nullToEmpty(employee.getName())).append("\n");
        prompt.append("- 部门：").append(nullToEmpty(employee.getDeptName())).append("\n");
        prompt.append("- 岗位：").append(nullToEmpty(employee.getPostName())).append("\n");
        prompt.append("- 职级：").append(nullToEmpty(employee.getLevelName())).append("\n\n");

        BigDecimal currentBasic = BigDecimal.ZERO;
        double foodSubsidy = 0, trafficSubsidy = 0, housingSubsidy = 0, levelSubsidy = 0;

        if (salary != null) {
            String monthStr = salary.getMonth() != null ? salary.getMonth().toString().substring(0, 7) : "未知";
            currentBasic = salary.getBasicSalary() != null ? salary.getBasicSalary() : BigDecimal.ZERO;
            foodSubsidy = salary.getFoodSubsidy() != null ? salary.getFoodSubsidy().doubleValue() : 0;
            trafficSubsidy = salary.getTrafficSubsidy() != null ? salary.getTrafficSubsidy().doubleValue() : 0;
            housingSubsidy = salary.getHousingSubsidy() != null ? salary.getHousingSubsidy().doubleValue() : 0;
            levelSubsidy = salary.getLevelSubsidy() != null ? salary.getLevelSubsidy().doubleValue() : 0;

            prompt.append("【当前月薪数据（").append(monthStr).append("月）】\n");
            prompt.append("- 基本工资：").append(formatBigDecimal(currentBasic)).append("元\n");
            prompt.append("- 餐补：").append(formatDouble(foodSubsidy)).append("元\n");
            prompt.append("- 交通补贴：").append(formatDouble(trafficSubsidy)).append("元\n");
            prompt.append("- 住房补贴：").append(formatDouble(housingSubsidy)).append("元\n");
            prompt.append("- 职级补贴：").append(formatDouble(levelSubsidy)).append("元\n");

            BigDecimal foodSubsidyBD = salary.getFoodSubsidy() != null ? salary.getFoodSubsidy() : BigDecimal.ZERO;
            BigDecimal trafficSubsidyBD = salary.getTrafficSubsidy() != null ? salary.getTrafficSubsidy() : BigDecimal.ZERO;
            BigDecimal housingSubsidyBD = salary.getHousingSubsidy() != null ? salary.getHousingSubsidy() : BigDecimal.ZERO;
            BigDecimal levelSubsidyBD = salary.getLevelSubsidy() != null ? salary.getLevelSubsidy() : BigDecimal.ZERO;
            BigDecimal totalMonthSalary = calculateTotalMonthSalary(currentBasic, foodSubsidyBD, trafficSubsidyBD, housingSubsidyBD, levelSubsidyBD);
            prompt.append("- 月应发工资合计：").append(formatBigDecimal(totalMonthSalary)).append("元\n\n");
        } else {
            prompt.append("【当前月薪数据】\n");
            prompt.append("- 暂无工资记录\n\n");
        }

        prompt.append("【年终奖信息】\n");
        prompt.append("- 当前年终奖金额：").append(formatDouble(yearEndBonus != null ? yearEndBonus : 0.0)).append("元\n\n");

        prompt.append("【计算规则】\n");
        prompt.append("1. 应发工资 = 基本工资 + 餐补 + 交通补贴 + 住房补贴 + 职级补贴\n");
        prompt.append("2. 应纳税所得额 = 应发工资 - 5000元起征点\n");
        prompt.append("3. 个税 = 应纳税所得额 × 税率 - 速算扣除数\n");
        prompt.append("4. 年终奖单独计税：年终奖 ÷ 12 确定税率，税额 = 年终奖 × 税率 - 速算扣除数\n");
        prompt.append("5. 税率临界点：36000元、144000元、300000元、420000元、660000元、960000元\n\n");

        prompt.append("【约束条件】\n");
        prompt.append("- 优化后的年度总收入必须等于当前年度总收入！不能多也不能少！\n");
        prompt.append("- 当前年度总收入 = (基本工资 + 餐补 + 交通补贴 + 住房补贴 + 职级补贴) × 12 + 年终奖\n");
        prompt.append("- 餐补、交通补贴、住房补贴、职级补贴是固定不变的，只能调整基本工资和年终奖！\n");
        prompt.append("- 计算公式：新基本工资 = (当前年度总收入 - 新年终奖) / 12 - (餐补 + 交通补贴 + 住房补贴 + 职级补贴)\n");
        prompt.append("- 例如：如果当前年度总收入是241560元，年终奖设为36000元，则基本工资 = (241560 - 36000) / 12 - 津贴\n\n");

        prompt.append("【任务】\n");
        prompt.append("请帮我找到最优的工资分配方案，目标是在年度总收入不变的前提下，通过调整基本工资和年终奖的比例，最小化个人所得税。\n\n");

        prompt.append("【已知数据】\n");
        prompt.append("- 当前基本工资：").append(formatBigDecimal(currentBasic)).append("元\n");
        prompt.append("- 餐补：").append(formatDouble(foodSubsidy)).append("元\n");
        prompt.append("- 交通补贴：").append(formatDouble(trafficSubsidy)).append("元\n");
        prompt.append("- 住房补贴：").append(formatDouble(housingSubsidy)).append("元\n");
        prompt.append("- 职级补贴：").append(formatDouble(levelSubsidy)).append("元\n");
        prompt.append("- 当前年终奖：").append(formatDouble(yearEndBonus != null ? yearEndBonus : 0.0)).append("元\n\n");

        prompt.append("【计算公式】\n");
        prompt.append("1. 年度总收入 = (基本工资 + 餐补 + 交通补贴 + 住房补贴 + 职级补贴) × 12 + 年终奖\n");
        prompt.append("2. 新基本工资 = (年度总收入 - 新年终奖) / 12 - (餐补 + 交通补贴 + 住房补贴 + 职级补贴)\n");
        prompt.append("3. 月度个税：应纳税所得额 = 应发工资 - 5000，按税率表计算\n");
        prompt.append("4. 年终奖个税：年终奖 ÷ 12 确定税率，税额 = 年终奖 × 税率 - 速算扣除数\n");
        prompt.append("5. 税率临界点：36000、144000、300000、420000、660000、960000\n\n");

        prompt.append("【请输出】\n");
        prompt.append("请直接输出JSON格式的最优方案，不要任何多余文字。\n");
        prompt.append("格式要求：{\"suggestedBasicSalary\":15000,\"suggestedBonus\":25000}\n");

        return prompt.toString();
    }

    private BigDecimal calculateTotalMonthSalary(BigDecimal basic, BigDecimal food, BigDecimal traffic, BigDecimal housing, BigDecimal level) {
        return basic.add(food).add(traffic).add(housing).add(level);
    }

    private Employee getEmployeeById(Integer empId) {
        Employee employee = employeeMapper.findById(empId);
        if (employee == null) {
            throw new IllegalArgumentException("未找到员工信息");
        }
        return employee;
    }

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.toString();
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    private String nullToEmpty(String str) {
        return str == null ? "未知" : str;
    }

    private HttpEntity<Map<String, Object>> buildRequestEntity(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + sparkAiConfig.getApiPassword());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "lite");
        requestBody.put("stream", false);
        requestBody.put("user", "user001");

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        requestBody.put("messages", messages);

        return new HttpEntity<>(requestBody, headers);
    }

                @SuppressWarnings("null")
                private String callSparkApi(String prompt) {
        HttpEntity<Map<String, Object>> entity = buildRequestEntity(prompt);
        log.info(LOG_API_CALL, "lite");

        // 获取URL并校验非空
        String apiUrl = sparkAiConfig.getApiUrl();
        if (apiUrl == null || apiUrl.isEmpty()) {
            log.error("讯飞AI API地址配置为空");
            throw new IllegalStateException("讯飞AI API地址未配置");
        }

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
        apiUrl,
        HttpMethod.POST,
        entity,
        new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
);

            log.info(LOG_API_RESPONSE, response.getBody());
            Map<String, Object> body = response.getBody();

            if (response.getStatusCode().value() == 200 && body != null) {
                if (body.containsKey("error")) {
                    Object errorObj = body.get("error");
                    if (errorObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> error = (Map<String, Object>) errorObj;
                        Object errorMsgObj = error.get("message");
                        String errorMsg = errorMsgObj instanceof String ? (String) errorMsgObj : "未知错误";
                        log.error(LOG_API_ERROR, errorMsg);
                        return "AI服务错误: " + errorMsg;
                    }
                }

                Object codeObj = body.get("code");
                Integer code = codeObj instanceof Integer ? (Integer) codeObj : null;
                if (code != null && code != 0) {
                    Object msgObj = body.get("message");
                    String msg = msgObj instanceof String ? (String) msgObj : null;
                    log.error(LOG_API_ERROR_CODE, code, msg);
                    return "AI服务错误[code=" + code + "]: " + (msg != null ? msg : "未知错误");
                }

                Object choicesObj = body.get("choices");
                if (choicesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                    if (!choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        if (firstChoice.containsKey("message")) {
                            Object msgObj = firstChoice.get("message");
                            if (msgObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> msg = (Map<String, Object>) msgObj;
                                Object content = msg.get("content");
                                return content instanceof String ? (String) content : "AI返回内容不是字符串";
                            }
                        }
                    }
                }
                return "AI返回格式异常";
            }
            return "AI服务调用失败: " + response.getStatusCode();
        } catch (Exception e) {
            log.error(LOG_API_EXCEPTION, e.getMessage(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
}
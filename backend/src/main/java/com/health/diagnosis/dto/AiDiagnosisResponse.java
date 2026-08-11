package com.health.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Python AI 服务预问诊响应模型。
 *
 * <p>AI 服务返回字段为 snake_case，通过 @JsonProperty 映射为 Java 驼峰风格。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiDiagnosisResponse {

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 结构化问诊结果 */
    private DiagnosisData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiagnosisData {

        /** 风险等级：low / mid / high */
        @JsonProperty("risk_level")
        private String riskLevel;

        /** 疑似疾病列表 */
        @JsonProperty("possible_diseases")
        private List<String> possibleDiseases;

        /** 建议就诊科室 */
        @JsonProperty("suggested_department")
        private String suggestedDepartment;

        /** 综合问诊建议 */
        private String advice;

        /** 免责声明 */
        private String disclaimer;
    }
}

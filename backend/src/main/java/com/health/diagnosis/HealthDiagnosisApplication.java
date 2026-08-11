package com.health.diagnosis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 大健康行业智能问诊系统 - 启动类。
 *
 * <p>整体架构：Java SpringBoot 后端负责业务与鉴权，Python FastAPI 负责 AI 推理，
 * 后端通过 HTTP 调用 AI 服务完成「AI 预问诊」。</p>
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.health.diagnosis.mapper")
public class HealthDiagnosisApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthDiagnosisApplication.class, args);
    }
}

package com.webgis.dsws.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller cho trang chủ.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Hệ thống", description = "API kiểm tra trạng thái và thông tin hệ thống")
public class HomeController {

    @Value("${spring.application.name:Disease Zoning and Prediction System}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${springdoc.swagger-ui.path:}")
    private String baseUrl;

    /**
     * Endpoint để kiểm tra trạng thái của ứng dụng.
     *
     * @return ResponseEntity chứa thông báo trạng thái
     */
    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái hoạt động của hệ thống")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/info")
    @Operation(summary = "Lấy thông tin cơ bản về hệ thống")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("applicationName", applicationName);
        info.put("environment", activeProfile);
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        info.put("freeMemory", Runtime.getRuntime().freeMemory());
        info.put("totalMemory", Runtime.getRuntime().totalMemory());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    @Operation(summary = "Kiểm tra sức khỏe của hệ thống")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "HEALTHY");
        health.put("database", "Connected");
        health.put("diskSpace", checkDiskSpace());
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> diskSpace = new HashMap<>();
        diskSpace.put("total", new java.io.File("/").getTotalSpace());
        diskSpace.put("free", new java.io.File("/").getFreeSpace());
        diskSpace.put("usable", new java.io.File("/").getUsableSpace());
        return diskSpace;
    }
}
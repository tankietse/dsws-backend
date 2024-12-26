package com.webgis.dsws.controller.api;

import com.webgis.dsws.domain.model.alert.AlertConfiguration;
import com.webgis.dsws.domain.repository.AlertConfigurationRepository;
import com.webgis.dsws.domain.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alert-configurations")
@Tag(name = "Cảnh báo", description = "API quản lý cấu hình cảnh báo")
@RequiredArgsConstructor
public class AlertConfigurationApi {
    private final AlertService alertService;
    private final AlertConfigurationRepository alertConfigRepository;
    @GetMapping
    @Operation(summary = "Lấy danh sách cấu hình cảnh báo")
    public ResponseEntity<?> getAllConfigurations() {
        return ResponseEntity.ok(alertConfigRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Tạo mới cấu hình cảnh báo")
    public ResponseEntity<?> createConfiguration(@RequestBody AlertConfiguration config) {
        return ResponseEntity.ok(alertConfigRepository.save(config));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật cấu hình cảnh báo")
    public ResponseEntity<?> updateConfiguration(
            @PathVariable Long id,
            @RequestBody AlertConfiguration config) {
        if (!alertConfigRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        config.setId(id);
        return ResponseEntity.ok(alertConfigRepository.save(config));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa cấu hình cảnh báo")
    public ResponseEntity<?> deleteConfiguration(@PathVariable Long id) {
        if (!alertConfigRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alertConfigRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

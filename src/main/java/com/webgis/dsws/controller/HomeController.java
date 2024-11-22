package com.webgis.dsws.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

/**
 * Controller cho trang chủ.
 */
@RestController
@RequestMapping("/api/v1/")
public class HomeController {

    @Value("${springdoc.swagger-ui.path:}")
    private String baseUrl;

    /**
     * Endpoint để kiểm tra trạng thái của ứng dụng.
     *
     * @return ResponseEntity chứa thông báo trạng thái
     */
    @GetMapping("/helloworld")
    public ResponseEntity<String> getStatus() {
        String message = "<h1>Disease Zoning and Prediction System - Backend Spring Boot is running</h1>";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
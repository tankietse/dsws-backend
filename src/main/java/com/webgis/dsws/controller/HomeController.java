package com.webgis.dsws.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/status")
    public String getStatus() {
        return "<h1>WebGIS is running</h1>";
    }
}
package com.webgis.dsws.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome to Thymeleaf!");
        return "home/index";
    }

    @GetMapping("/ql-dia-gioi")
    public String index(Model model) {
        return "ql-dia-gioi/ql-donvihanhchinh";
    }

    @GetMapping("/ql-trang-trai")
    public String trangTrai(Model model) {
        return "trangtrai/ql-trangtrai";
    }
}
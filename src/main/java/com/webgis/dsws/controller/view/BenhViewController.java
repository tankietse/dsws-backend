package com.webgis.dsws.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/benh")
public class BenhViewController {
    
    @GetMapping
    public String listBenh() {
        return "benh/list";
    }

    @GetMapping("/create")
    public String createBenhForm() {
        return "benh/create";
    }

    @GetMapping("/edit/{id}")
    public String editBenhForm() {
        return "benh/edit";
    }
}
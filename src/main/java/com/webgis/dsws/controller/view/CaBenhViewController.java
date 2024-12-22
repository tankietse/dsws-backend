package com.webgis.dsws.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ca-benh")
public class CaBenhViewController {

    @GetMapping("/list")
    public String danhSachCaBenh() {
        return "ca-benh/list";
    }

    @GetMapping("/create")
    public String showCreateForm() {
        return "ca-benh/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm() {
        return "ca-benh/edit";
    }

    @GetMapping("/details/{id}")
    public String showDetails() {
        return "ca-benh/details";
    }
}
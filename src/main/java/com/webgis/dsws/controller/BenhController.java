package com.webgis.dsws.controller;

import com.webgis.dsws.model.Benh;
import com.webgis.dsws.repository.BenhRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/benh")
public class BenhController {

    @Autowired
    private BenhRepository benhRepository;

    // Hiển thị danh sách bệnh
    @GetMapping
    public String listBenh(Model model) {
        List<Benh> dsBenh = benhRepository.findAll();
        model.addAttribute("dsBenh", dsBenh);
        return "benh/list";
    }

    // Hiển thị form thêm bệnh
    @GetMapping("/create")
    public String createBenhForm(Model model) {
        model.addAttribute("benh", new Benh());
        return "benh/create";
    }

    // Xử lý thêm bệnh
    @PostMapping("/create")
    public String createBenh(@Valid @ModelAttribute("benh") Benh benh, BindingResult result) {
        if (result.hasErrors()) {
            return "benh/create";
        }
        benhRepository.save(benh);
        return "redirect:/benh";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String editBenhForm(@PathVariable Long id, Model model) {
        Benh benh = benhRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
        model.addAttribute("benh", benh);
        return "benh/edit";
    }

    // Xử lý chỉnh sửa
    @PostMapping("/edit/{id}")
    public String editBenh(@PathVariable Long id, @Valid @ModelAttribute("benh") Benh benh, BindingResult result) {
        if (result.hasErrors()) {
            return "benh/edit";
        }
        benhRepository.save(benh);
        return "redirect:/benh";
    }

    // Xóa bệnh
    @GetMapping("/delete/{id}")
    public String deleteBenh(@PathVariable Long id) {
        Benh benh = benhRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Benh ID: " + id));
        benhRepository.delete(benh);
        return "redirect:/benh";
    }
}
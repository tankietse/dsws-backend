package com.webgis.dsws.controller;

import com.webgis.dsws.model.NguoiDung;
import com.webgis.dsws.model.dto.NguoiDungDTO;
import com.webgis.dsws.service.NguoiDungService;
import com.webgis.dsws.service.VaiTroService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private NguoiDungService userService;

    @Autowired
    private VaiTroService roleService;

    @GetMapping("/index")
    public String home() {
        return "/index";
    }

    @GetMapping("/login")
    public String login() {
        return "users/login";
    }
    
    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new NguoiDung());
        return "users/register";
    }

//    @PostMapping("/register")
//    public String register(@ModelAttribute NguoiDungDTO userDto) {
//        userService.save(userDto);
//        userService.setDefaultRole(userDto.getTenDangNhap());
//        return "redirect:/login";
//    }

//    @GetMapping("/profile")
//    public String profile(@NotNull Model model) {
//        User currentUser = userService.getCurrentUser();
//        NhanVien nhanVien = currentUser.getNhanVien();
//        model.addAttribute("nhanVien", nhanVien);
//        return "users/profile";
//    }

//    @GetMapping("/devFunc")
//    public String devFunc() {
//        return "developFunc";
//    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Hủy phiên HttpSession
        SecurityContextHolder.clearContext(); // Xóa thông tin xác thực
        return "redirect:/login";
    }
}

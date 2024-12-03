package com.webgis.dsws.controller.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webgis.dsws.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeViewController {
    private final JwtUtil jwtUtil;

    public HomeViewController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/")
    public String home(Model model) {
        log.info("Handling home page request");

        // Access the authenticated user from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            return "home/index";
        }

        log.info("Unauthenticated access, redirecting to login");
        return "redirect:/auth/login";
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
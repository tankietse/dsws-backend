package com.webgis.dsws.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/auth")
public class AuthViewController {

    @GetMapping({ "/login", "/register" })
    public String authPage() {
        return "auth/auth-page";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "auth/profile";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public String userManagementPage() {
        return "auth/account";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users/add")
    public String addUserPage() {
        return "auth/user-add";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users/edit/{id}")
    public String editUserPage(@PathVariable Long id) {
        return "auth/user-edit";
    }
}
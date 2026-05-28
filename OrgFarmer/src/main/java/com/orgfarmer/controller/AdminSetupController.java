package com.orgfarmer.controller;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;

@Controller
public class AdminSetupController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/setup-admin")
    public String showSetupForm(Model model) {
        // Check if admin already exists
        if (userService.isAdminExists()) {
            return "redirect:/login?error=Admin already exists";
        }
        return "setup-admin";
    }
    
    @PostMapping("/setup-admin")
    public String createAdmin(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String phone,
                              Model model) {
        try {
            // Check if admin already exists (double check)
            if (userService.isAdminExists()) {
                model.addAttribute("error", "Admin account already exists!");
                return "setup-admin";
            }
            
            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPassword(password);
            admin.setPhone(phone);
            admin.setUserType(UserType.ADMIN);
            admin.setAddress("System Administrator");
            
            userService.registerUser(admin);
            
            return "redirect:/login?success=Admin account created successfully! Please login.";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "setup-admin";
        }
    }
}
package com.orgfarmer.controller;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("userTypes", UserType.values());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, 
                               BindingResult result, 
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("userTypes", UserType.values());
            return "register";
        }
        
        try {
            userService.registerUser(user);
            return "redirect:/login?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userTypes", UserType.values());
            return "register";
        }
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    
    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        try {
            User user = userService.authenticateUser(email, password);
            session.setAttribute("loggedInUser", user);
            
            if (user.getUserType() == UserType.ADMIN) {
                return "redirect:/admin/dashboard";
            } else if (user.getUserType() == UserType.SELLER) {
                return "redirect:/seller/dashboard";
            } else {
                return "redirect:/customer/dashboard";
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
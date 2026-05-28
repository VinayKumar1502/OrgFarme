package com.orgfarmer.controller;

import com.orgfarmer.model.*;
import com.orgfarmer.repository.*;
import com.orgfarmer.service.ProductService;
import com.orgfarmer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired private UserService userService;
    @Autowired private ProductService productService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ContactMessageRepository contactMessageRepository;
    
    // Check if logged in user is ADMIN
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && user.getUserType() == UserType.ADMIN;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        User admin = (User) session.getAttribute("loggedInUser");
        
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalSellers", userRepository.findByUserType(UserType.SELLER).size());
        model.addAttribute("totalCustomers", userRepository.findByUserType(UserType.CUSTOMER).size());
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalReviews", reviewRepository.count());
        model.addAttribute("pendingMessages", contactMessageRepository.countByStatus("PENDING"));
        model.addAttribute("recentUsers", userRepository.findAll().stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("admin", admin);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/users")
    public String users(@RequestParam(required = false) String type, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        if ("SELLER".equals(type)) {
            model.addAttribute("users", userRepository.findByUserType(UserType.SELLER));
            model.addAttribute("pageTitle", "All Sellers");
        } else if ("CUSTOMER".equals(type)) {
            model.addAttribute("users", userRepository.findByUserType(UserType.CUSTOMER));
            model.addAttribute("pageTitle", "All Customers");
        } else {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("pageTitle", "All Users");
        }
        
        return "admin/users";
    }
    
    @GetMapping("/products")
    public String products(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        model.addAttribute("products", productRepository.findAll());
        return "admin/products";
    }
    
    @GetMapping("/reviews")
    public String reviews(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        model.addAttribute("reviews", reviewRepository.findAll());
        return "admin/reviews";
    }
    
    @GetMapping("/messages")
    public String messages(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        model.addAttribute("messages", contactMessageRepository.findAll());
        model.addAttribute("pendingCount", contactMessageRepository.countByStatus("PENDING"));
        return "admin/messages";
    }
    
    @PostMapping("/user/toggle/{id}")
    public String toggleUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        User user = userService.getUserById(id);
        if (user != null && user.getUserType() != UserType.ADMIN) {
            user.setActive(!user.isActive());
            userRepository.save(user);
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/message/resolve")
    public String resolveMessage(@RequestParam Long messageId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login?error=Unauthorized access";
        }
        
        ContactMessage msg = contactMessageRepository.findById(messageId).orElse(null);
        if (msg != null) {
            msg.setStatus("RESOLVED");
            contactMessageRepository.save(msg);
        }
        return "redirect:/admin/messages";
    }
    
    @DeleteMapping("/message/delete/{id}")
    @ResponseBody
    public String deleteMessage(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "Unauthorized";
        }
        
        contactMessageRepository.deleteById(id);
        return "Success";
    }
    
    @DeleteMapping("/product/delete/{id}")
    @ResponseBody
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "Unauthorized";
        }
        
        productRepository.deleteById(id);
        return "Success";
    }
    
    @DeleteMapping("/review/delete/{id}")
    @ResponseBody
    public String deleteReview(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) {
            return "Unauthorized";
        }
        
        reviewRepository.deleteById(id);
        return "Success";
    }
}
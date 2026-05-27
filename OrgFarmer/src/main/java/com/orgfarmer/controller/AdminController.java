package com.orgfarmer.controller;

import com.orgfarmer.model.*;
import com.orgfarmer.repository.*;
import com.orgfarmer.service.UserService;
import com.orgfarmer.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private ContactMessageRepository contactMessageRepository;
    
    /**
     * Admin Dashboard - Main Page
     */
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        // Get statistics for dashboard
        long totalUsers = userRepository.count();
        long totalSellers = userRepository.findByUserType(UserType.SELLER).size();
        long totalCustomers = userRepository.findByUserType(UserType.CUSTOMER).size();
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findByAvailableTrue().size();
        long totalReviews = reviewRepository.count();
        long pendingMessages = contactMessageRepository.countByStatus("PENDING");
        
        // Get recent users (last 5)
        List<User> recentUsers = userRepository.findAll().stream()
            .limit(5)
            .collect(Collectors.toList());
        
        // Get recent products
        List<Product> recentProducts = productRepository.findAll().stream()
            .limit(5)
            .collect(Collectors.toList());
        
        // Get top selling products
        List<Product> topProducts = productService.getTopSellingProducts(5);
        
        model.addAttribute("admin", admin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalSellers", totalSellers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("pendingMessages", pendingMessages);
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("recentProducts", recentProducts);
        model.addAttribute("topProducts", topProducts);
        
        return "admin/dashboard";
    }
    
    /**
     * View all users
     */
    @GetMapping("/users")
    public String viewAllUsers(@RequestParam(required = false) String type, 
                               HttpSession session, 
                               Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        List<User> users;
        String pageTitle = "All Users";
        
        if ("SELLER".equals(type)) {
            users = userRepository.findByUserType(UserType.SELLER);
            pageTitle = "All Sellers/Farmers";
        } else if ("CUSTOMER".equals(type)) {
            users = userRepository.findByUserType(UserType.CUSTOMER);
            pageTitle = "All Customers";
        } else {
            users = userRepository.findAll();
            pageTitle = "All Users";
        }
        
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("selectedType", type);
        
        return "admin/users";
    }
    
    /**
     * View single user details
     */
    @GetMapping("/user/{id}")
    public String viewUserDetails(@PathVariable Long id, HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin/users?error=User not found";
        }
        
        // Get user statistics
        long productCount = 0;
        long reviewCount = 0;
        double avgRating = 0;
        
        if (user.getUserType() == UserType.SELLER) {
            productCount = productRepository.findBySellerId(id).size();
        } else if (user.getUserType() == UserType.CUSTOMER) {
            reviewCount = reviewRepository.findByCustomerId(id).size();
        }
        
        model.addAttribute("user", user);
        model.addAttribute("productCount", productCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("avgRating", avgRating);
        
        return "admin/user-details";
    }
    
    /**
     * Activate/Deactivate user account
     */
    @PostMapping("/user/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(id);
        if (user != null && user.getUserType() != UserType.ADMIN) {
            user.setActive(!user.isActive());
            userRepository.save(user);
        }
        
        return "redirect:/admin/users";
    }
    
    /**
     * Delete user account
     */
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(id);
        if (user != null && user.getUserType() != UserType.ADMIN) {
            userRepository.delete(user);
        }
        
        return "redirect:/admin/users?success=User deleted successfully";
    }
    
    /**
     * View all products
     */
    @GetMapping("/products")
    public String viewAllProducts(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        List<Product> products = productRepository.findAll();
        List<String> categories = productService.getAllCategories();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("activeProducts", products.stream().filter(Product::isAvailable).count());
        
        return "admin/products";
    }
    
    /**
     * View products by category
     */
    @GetMapping("/products/category/{category}")
    public String viewProductsByCategory(@PathVariable String category, 
                                        HttpSession session, 
                                        Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        List<Product> products = productRepository.findByCategory(category);
        
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        model.addAttribute("totalProducts", products.size());
        
        return "admin/products-by-category";
    }
    
    /**
     * Approve/Reject product (for quality check)
     */
    @PostMapping("/product/approve/{id}")
    public String approveProduct(@PathVariable Long id, 
                                @RequestParam boolean approve,
                                HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        Product product = productService.getProductById(id);
        if (product != null) {
            product.setAvailable(approve);
            productRepository.save(product);
        }
        
        return "redirect:/admin/products";
    }
    
    /**
     * Delete product
     */
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        productRepository.deleteById(id);
        
        return "redirect:/admin/products?success=Product deleted";
    }
    
    /**
     * View all reviews
     */
    @GetMapping("/reviews")
    public String viewAllReviews(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        List<Review> reviews = reviewRepository.findAll();
        
        // Calculate average rating
        double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", reviews.size());
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        
        return "admin/reviews";
    }
    
    /**
     * Delete review
     */
    @GetMapping("/review/delete/{id}")
    public String deleteReview(@PathVariable Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        reviewRepository.deleteById(id);
        
        return "redirect:/admin/reviews?success=Review deleted";
    }
    
    /**
     * View contact messages
     */
    @GetMapping("/messages")
    public String viewMessages(@RequestParam(required = false) String status,
                              HttpSession session, 
                              Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        List<ContactMessage> messages;
        if ("PENDING".equals(status)) {
            messages = contactMessageRepository.findByStatus("PENDING");
        } else if ("RESOLVED".equals(status)) {
            messages = contactMessageRepository.findByStatus("RESOLVED");
        } else {
            messages = contactMessageRepository.findAll();
        }
        
        long pendingCount = contactMessageRepository.countByStatus("PENDING");
        
        model.addAttribute("messages", messages);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("selectedStatus", status);
        
        return "admin/messages";
    }
    
    /**
     * View single message
     */
    @GetMapping("/message/{id}")
    public String viewMessage(@PathVariable Long id, HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        ContactMessage message = contactMessageRepository.findById(id).orElse(null);
        if (message == null) {
            return "redirect:/admin/messages";
        }
        
        model.addAttribute("message", message);
        
        return "admin/message-details";
    }
    
    /**
     * Mark message as resolved
     */
    @PostMapping("/message/resolve/{id}")
    public String resolveMessage(@PathVariable Long id, HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "redirect:/login";
        }
        
        ContactMessage message = contactMessageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setStatus("RESOLVED");
            contactMessageRepository.save(message);
        }
        
        return "redirect:/admin/messages";
    }
    
    /**
     * Get statistics/charts data (for dashboard charts)
     */
    @GetMapping("/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return new HashMap<>();
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSellers", userRepository.findByUserType(UserType.SELLER).size());
        stats.put("totalCustomers", userRepository.findByUserType(UserType.CUSTOMER).size());
        
        // Product statistics
        stats.put("totalProducts", productRepository.count());
        stats.put("activeProducts", productRepository.findByAvailableTrue().size());
        
        // Review statistics
        stats.put("totalReviews", reviewRepository.count());
        
        // Category wise product count
        List<String> categories = productService.getAllCategories();
        Map<String, Long> categoryCount = new HashMap<>();
        for (String category : categories) {
            categoryCount.put(category, (long) productRepository.findByCategory(category).size());
        }
        stats.put("categoryCount", categoryCount);
        
        return stats;
    }
    
    /**
     * Export users data to CSV
     */
    @GetMapping("/export/users")
    @ResponseBody
    public String exportUsers(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInUser");
        if (admin == null || admin.getUserType() != UserType.ADMIN) {
            return "Unauthorized";
        }
        
        List<User> users = userRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Email,Phone,UserType,Address,RegistrationDate,Active\n");
        
        for (User user : users) {
            csv.append(user.getId()).append(",")
               .append(user.getName()).append(",")
               .append(user.getEmail()).append(",")
               .append(user.getPhone()).append(",")
               .append(user.getUserType()).append(",")
               .append(user.getAddress() != null ? user.getAddress().replace(",", " ") : "").append(",")
               .append(user.getRegistrationDate()).append(",")
               .append(user.isActive()).append("\n");
        }
        
        return csv.toString();
    }
}
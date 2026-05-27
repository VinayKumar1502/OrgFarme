package com.orgfarmer.controller;

import com.orgfarmer.model.Product;
import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.repository.FavoriteRepository;
import com.orgfarmer.service.ProductService;
import com.orgfarmer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @GetMapping("/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        User customer = (User) session.getAttribute("loggedInUser");
        if (customer == null || customer.getUserType() != UserType.CUSTOMER) {
            return "redirect:/login";
        }
        
        // Get nearby sellers (within 10km)
        List<User> nearbySellers = userService.getNearbySellers(
            customer.getLatitude(), customer.getLongitude(), 10.0);
        
        List<Product> nearbyProducts = productService.getProductsBySellers(nearbySellers);
        List<Product> topProducts = productService.getTopSellingProducts(10);
        List<String> categories = productService.getAllCategories();
        
        model.addAttribute("products", nearbyProducts);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("customer", customer);
        
        return "customer/dashboard";
    }
    
    @GetMapping("/product/{id}")
    public String viewProduct(@PathVariable Long id, HttpSession session, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/customer/dashboard";
        }
        
        User customer = (User) session.getAttribute("loggedInUser");
        
        double distance = calculateDistance(
            customer.getLatitude(), customer.getLongitude(),
            product.getSeller().getLatitude(), product.getSeller().getLongitude());
        
        Double avgRating = productService.getAverageRating(id);
        Long reviewCount = productService.getReviewCount(id);
        
        model.addAttribute("product", product);
        model.addAttribute("distance", Math.round(distance * 100.0) / 100.0);
        model.addAttribute("avgRating", avgRating != null ? avgRating : 0);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reviews", product.getReviews());
        
        return "customer/product-details";
    }
    
    @PostMapping("/review")
    public String addReview(@RequestParam Long productId,
                           @RequestParam Integer rating,
                           @RequestParam String comment,
                           HttpSession session) {
        User customer = (User) session.getAttribute("loggedInUser");
        productService.addReview(productId, customer.getId(), rating, comment);
        return "redirect:/customer/product/" + productId;
    }
    
    @PostMapping("/favorite/add/{sellerId}")
    public String addToFavorites(@PathVariable Long sellerId, HttpSession session) {
        User customer = (User) session.getAttribute("loggedInUser");
        favoriteRepository.addFavorite(customer.getId(), sellerId);
        return "redirect:/customer/dashboard";
    }
    
    @PostMapping("/favorite/remove/{sellerId}")
    public String removeFromFavorites(@PathVariable Long sellerId, HttpSession session) {
        User customer = (User) session.getAttribute("loggedInUser");
        favoriteRepository.removeFavorite(customer.getId(), sellerId);
        return "redirect:/customer/favorites";
    }
    
    @GetMapping("/favorites")
    public String viewFavorites(HttpSession session, Model model) {
        User customer = (User) session.getAttribute("loggedInUser");
        List<User> favoriteSellers = favoriteRepository.findFavoriteSellersByCustomerId(customer.getId());
        model.addAttribute("favorites", favoriteSellers);
        return "customer/favorites";
    }
    
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String keyword, Model model) {
        List<Product> searchResults = productService.searchProducts(keyword);
        model.addAttribute("products", searchResults);
        model.addAttribute("keyword", keyword);
        return "customer/search-results";
    }
    
    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
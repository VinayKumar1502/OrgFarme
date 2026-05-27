package com.orgfarmer.controller;

import com.orgfarmer.model.Product;
import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/seller")
public class SellerController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        
        List<Product> products = productService.getProductsBySeller(seller.getId());
        model.addAttribute("products", products);
        model.addAttribute("seller", seller);
        model.addAttribute("productCount", products.size());
        return "seller/dashboard";
    }
    
    @GetMapping("/add-product")
    public String showAddProductForm(Model model, HttpSession session) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        model.addAttribute("product", new Product());
        return "seller/add-product";
    }
    
    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute Product product,
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            User seller = (User) session.getAttribute("loggedInUser");
            if (seller == null || seller.getUserType() != UserType.SELLER) {
                return "redirect:/login";
            }
            
            // Validate required fields
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product name is required");
                return "redirect:/seller/add-product";
            }
            
            if (product.getPrice() == null || product.getPrice() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Valid price is required");
                return "redirect:/seller/add-product";
            }
            
            if (product.getQuantity() == null || product.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Valid quantity is required");
                return "redirect:/seller/add-product";
            }
            
            // Save product
            productService.addProduct(product, seller, image);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
            return "redirect:/seller/dashboard";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
            return "redirect:/seller/add-product";
        }
    }
    
    @GetMapping("/product/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model, HttpSession session) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        
        Product product = productService.getProductById(id);
        if (product == null || !product.getSeller().getId().equals(seller.getId())) {
            return "redirect:/seller/dashboard";
        }
        
        model.addAttribute("product", product);
        return "seller/edit-product";
    }
    
    @PostMapping("/product/update")
    public String updateProduct(@ModelAttribute Product product, HttpSession session, RedirectAttributes redirectAttributes) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        
        try {
            productService.updateProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product");
        }
        return "redirect:/seller/dashboard";
    }
    
    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product");
        }
        return "redirect:/seller/dashboard";
    }
    
    @GetMapping("/update-location")
    public String showUpdateLocationForm(Model model, HttpSession session) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        model.addAttribute("seller", seller);
        return "seller/update-location";
    }
    
    @PostMapping("/update-location")
    public String updateLocation(@RequestParam Double latitude,
                                @RequestParam Double longitude,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User seller = (User) session.getAttribute("loggedInUser");
        if (seller == null || seller.getUserType() != UserType.SELLER) {
            return "redirect:/login";
        }
        
        seller.setLatitude(latitude);
        seller.setLongitude(longitude);
        redirectAttributes.addFlashAttribute("success", "Location updated successfully!");
        return "redirect:/seller/dashboard";
    }
}
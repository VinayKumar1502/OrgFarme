package com.orgfarmer.service;

import com.orgfarmer.model.Product;
import com.orgfarmer.model.Review;
import com.orgfarmer.model.User;
import com.orgfarmer.repository.ProductRepository;
import com.orgfarmer.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    
    // Add product with image
    public Product addProduct(Product product, User seller, MultipartFile image) {
        try {
            product.setSeller(seller);
            product.setAddedDate(new Date());
            product.setAvailable(true);
            product.setSoldCount(0L);
            
            // Handle image upload if present
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                
                // Create directory if not exists
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, image.getBytes());
                product.setImageUrl("/uploads/" + fileName);
            }
            
            return productRepository.save(product);
        } catch (IOException e) {
            e.printStackTrace();
            // Still save product even if image upload fails
            return productRepository.save(product);
        }
    }
    
    // Simple add product without image
    public Product addProductSimple(Product product, User seller) {
        product.setSeller(seller);
        product.setAddedDate(new Date());
        product.setAvailable(true);
        product.setSoldCount(0L);
        return productRepository.save(product);
    }
    
    public List<Product> getProductsBySellers(List<User> sellers) {
        if (sellers == null || sellers.isEmpty()) {
            return productRepository.findByAvailableTrue();
        }
        List<Long> sellerIds = sellers.stream()
            .map(User::getId)
            .collect(java.util.stream.Collectors.toList());
        return productRepository.findProductsBySellers(sellerIds);
    }
    
    public List<Product> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, limit));
    }
    
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findByAvailableTrue();
        }
        return productRepository.searchProducts(keyword);
    }
    
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
    
    public void addReview(Long productId, Long customerId, Integer rating, String comment) {
        Product product = getProductById(productId);
        if (product != null) {
            Review review = new Review();
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);
            review.setReviewDate(new Date());
            reviewRepository.save(review);
        }
    }
    
    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }
    
    public Long getReviewCount(Long productId) {
        return reviewRepository.getReviewCountByProductId(productId);
    }
    
    public Product updateProduct(Product product) {
        Product existing = getProductById(product.getId());
        if (existing != null) {
            existing.setName(product.getName());
            existing.setCategory(product.getCategory());
            existing.setPrice(product.getPrice());
            existing.setQuantity(product.getQuantity());
            existing.setDescription(product.getDescription());
            existing.setAvailable(product.isAvailable());
            return productRepository.save(existing);
        }
        return null;
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
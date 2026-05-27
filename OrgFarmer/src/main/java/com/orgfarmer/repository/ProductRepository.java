package com.orgfarmer.repository;

import com.orgfarmer.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findBySellerId(Long sellerId);
    
    List<Product> findByCategory(String category);
    
    List<Product> findByAvailableTrue();
    
    @Query("SELECT p FROM Product p WHERE p.available = true ORDER BY p.soldCount DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.available = true AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    List<Product> searchProducts(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.category != ''")
    List<String> findAllCategories();
    
    @Query("SELECT p FROM Product p WHERE p.available = true AND p.seller.id IN :sellerIds")
    List<Product> findProductsBySellers(@Param("sellerIds") List<Long> sellerIds);
    
    long countBySellerId(Long sellerId);
}
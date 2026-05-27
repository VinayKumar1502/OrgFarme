package com.orgfarmer.repository;

import com.orgfarmer.model.Favorite;
import com.orgfarmer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Query("SELECT f.seller FROM Favorite f WHERE f.customer.id = :customerId AND f.active = true")
    List<User> findFavoriteSellersByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT f FROM Favorite f WHERE f.customer.id = :customerId AND f.seller.id = :sellerId")
    Favorite findByCustomerIdAndSellerId(@Param("customerId") Long customerId, @Param("sellerId") Long sellerId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Favorite f SET f.active = true WHERE f.customer.id = :customerId AND f.seller.id = :sellerId")
    void addFavorite(@Param("customerId") Long customerId, @Param("sellerId") Long sellerId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Favorite f SET f.active = false WHERE f.customer.id = :customerId AND f.seller.id = :sellerId")
    void removeFavorite(@Param("customerId") Long customerId, @Param("sellerId") Long sellerId);
}
package com.orgfarmer.repository;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByUserType(UserType userType);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.userType = 'SELLER' AND u.active = true AND u.latitude IS NOT NULL")
    List<User> findAllActiveSellersWithLocation();
    
    @Query("SELECT u FROM User u WHERE u.userType = 'SELLER' AND u.id IN :sellerIds")
    List<User> findSellersByIds(@Param("sellerIds") List<Long> sellerIds);
}
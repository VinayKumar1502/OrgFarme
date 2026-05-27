package com.orgfarmer.service;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationService locationService;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(new Date());
        return userRepository.save(user);
    }
    
    public User authenticateUser(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new Exception("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password");
        }
        return user;
    }
    
    public List<User> getNearbySellers(Double customerLat, Double customerLng, Double radiusKm) {
        // Using LocationService for nearby sellers calculation
        return locationService.getNearbySellers(customerLat, customerLng, radiusKm);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<User> getAllSellers() {
        return userRepository.findByUserType(UserType.SELLER);
    }
    
    public void updateUserLocation(Long userId, Double latitude, Double longitude) {
        if (locationService.isValidCoordinates(latitude, longitude)) {
            User user = getUserById(userId);
            if (user != null) {
                user.setLatitude(latitude);
                user.setLongitude(longitude);
                userRepository.save(user);
            }
        }
    }
    
    public String getDistanceFromCustomer(Long sellerId, Double customerLat, Double customerLng) {
        User seller = getUserById(sellerId);
        if (seller != null) {
            return locationService.getFormattedDistance(customerLat, customerLng, seller);
        }
        return "Unknown";
    }
}
package com.orgfarmer.service;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationService locationService;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User registerUser(User user) throws Exception {
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new Exception("Email already registered!");
        }
        
        // RESTRICT: Only allow ONE ADMIN user
        if (user.getUserType() == UserType.ADMIN) {
            // Check if admin already exists
            List<User> existingAdmins = userRepository.findByUserType(UserType.ADMIN);
            if (!existingAdmins.isEmpty()) {
                throw new Exception("Admin account already exists! Only one admin is allowed.");
            }
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(new Date());
        user.setActive(true);
        
        // Set default location if not provided
        if (user.getLatitude() == null) {
            user.setLatitude(0.0);
        }
        if (user.getLongitude() == null) {
            user.setLongitude(0.0);
        }
        
        return userRepository.save(user);
    }
    
    public User authenticateUser(String email, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (!userOptional.isPresent()) {
            throw new Exception("User not found with email: " + email);
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception("Invalid password!");
        }
        
        if (!user.isActive()) {
            throw new Exception("Account is deactivated. Contact admin.");
        }
        
        return user;
    }
    
    public List<User> getNearbySellers(Double customerLat, Double customerLng, Double radiusKm) {
        if (customerLat == null || customerLng == null) {
            return getAllSellers();
        }
        return locationService.getNearbySellers(customerLat, customerLng, radiusKm);
    }
    
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }
    
    public List<User> getAllSellers() {
        return userRepository.findByUserType(UserType.SELLER);
    }
    
    public List<User> getAllCustomers() {
        return userRepository.findByUserType(UserType.CUSTOMER);
    }
    
    public User getAdminUser() {
        List<User> admins = userRepository.findByUserType(UserType.ADMIN);
        return admins.isEmpty() ? null : admins.get(0);
    }
    
    public boolean isAdminExists() {
        return !userRepository.findByUserType(UserType.ADMIN).isEmpty();
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
        if (seller != null && customerLat != null && customerLng != null) {
            return locationService.getFormattedDistance(customerLat, customerLng, seller);
        }
        return "Location not set";
    }
    
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        if (user != null && user.getUserType() != UserType.ADMIN) {
            user.setActive(false);
            userRepository.save(user);
        }
    }
    
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user != null && user.getUserType() != UserType.ADMIN) {
            userRepository.delete(user);
        }
    }
}
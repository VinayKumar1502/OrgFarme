package com.orgfarmer.service;

import com.orgfarmer.model.User;
import com.orgfarmer.model.UserType;
import com.orgfarmer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate distance between two geographical points using Haversine formula
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Differences in coordinates
        double dlat = lat2Rad - lat1Rad;
        double dlon = lon2Rad - lon1Rad;
        
        // Haversine formula
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dlon / 2) * Math.sin(dlon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Get all sellers within specified radius from customer location
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @param radiusKm Radius in kilometers
     * @return List of sellers within radius
     */
    public List<User> getNearbySellers(Double customerLat, Double customerLng, Double radiusKm) {
        List<User> allSellers = userRepository.findByUserType(UserType.SELLER);
        
        return allSellers.stream()
            .filter(seller -> seller.getLatitude() != null && seller.getLongitude() != null)
            .filter(seller -> calculateDistance(customerLat, customerLng, 
                seller.getLatitude(), seller.getLongitude()) <= radiusKm)
            .collect(Collectors.toList());
    }
    
    /**
     * Get sellers sorted by distance from customer
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @return List of sellers sorted by nearest first
     */
    public List<User> getSellersSortedByDistance(Double customerLat, Double customerLng) {
        List<User> allSellers = userRepository.findByUserType(UserType.SELLER);
        
        return allSellers.stream()
            .filter(seller -> seller.getLatitude() != null && seller.getLongitude() != null)
            .sorted((s1, s2) -> {
                double dist1 = calculateDistance(customerLat, customerLng, 
                    s1.getLatitude(), s1.getLongitude());
                double dist2 = calculateDistance(customerLat, customerLng, 
                    s2.getLatitude(), s2.getLongitude());
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a seller is within specified radius
     * @param seller Seller user
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @param radiusKm Radius in kilometers
     * @return true if within radius, false otherwise
     */
    public boolean isSellerWithinRadius(User seller, Double customerLat, Double customerLng, Double radiusKm) {
        if (seller.getLatitude() == null || seller.getLongitude() == null) {
            return false;
        }
        
        double distance = calculateDistance(customerLat, customerLng, 
            seller.getLatitude(), seller.getLongitude());
        return distance <= radiusKm;
    }
    
    /**
     * Get distance between customer and seller with formatted output
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @param seller Seller user
     * @return Formatted distance string (e.g., "5.2 km")
     */
    public String getFormattedDistance(Double customerLat, Double customerLng, User seller) {
        if (customerLat == null || customerLng == null || 
            seller.getLatitude() == null || seller.getLongitude() == null) {
            return "Unknown distance";
        }
        
        double distance = calculateDistance(customerLat, customerLng, 
            seller.getLatitude(), seller.getLongitude());
        
        if (distance < 1) {
            return String.format("%.0f meters", distance * 1000);
        } else {
            return String.format("%.1f km", distance);
        }
    }
    
    /**
     * Validate if coordinates are within valid ranges
     * @param latitude Latitude to validate
     * @param longitude Longitude to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
    
    /**
     * Get approximate location name from coordinates (mock implementation)
     * In production, you would use reverse geocoding API like Google Maps or OpenStreetMap
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Location name or coordinates as string
     */
    public String getLocationName(Double latitude, Double longitude) {
        if (!isValidCoordinates(latitude, longitude)) {
            return "Location not set";
        }
        
        // This is a simplified mock implementation
        // For production, integrate with reverse geocoding API
        
        // Example: You can use OpenStreetMap Nominatim API
        // String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude;
        
        return String.format("(%.4f, %.4f)", latitude, longitude);
    }
    
    /**
     * Calculate delivery fee based on distance
     * @param distanceKm Distance in kilometers
     * @return Delivery fee
     */
    public double calculateDeliveryFee(double distanceKm) {
        if (distanceKm <= 0) return 0;
        if (distanceKm <= 5) return 20.0;
        if (distanceKm <= 10) return 40.0;
        if (distanceKm <= 20) return 70.0;
        return 100.0;
    }
    
    /**
     * Get nearby sellers count within radius
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @param radiusKm Radius in kilometers
     * @return Count of nearby sellers
     */
    public long getNearbySellersCount(Double customerLat, Double customerLng, Double radiusKm) {
        return getNearbySellers(customerLat, customerLng, radiusKm).size();
    }
    
    /**
     * Find the nearest seller to customer
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @return Nearest seller or null if no sellers exist
     */
    public User getNearestSeller(Double customerLat, Double customerLng) {
        List<User> sellersSorted = getSellersSortedByDistance(customerLat, customerLng);
        return sellersSorted.isEmpty() ? null : sellersSorted.get(0);
    }
    
    /**
     * Get sellers within multiple radius ranges
     * @param customerLat Customer's latitude
     * @param customerLng Customer's longitude
     * @return List of sellers with distance information
     */
    public List<SellerDistanceInfo> getSellersWithDistanceInfo(Double customerLat, Double customerLng) {
        List<User> sellers = userRepository.findByUserType(UserType.SELLER);
        
        return sellers.stream()
            .filter(seller -> seller.getLatitude() != null && seller.getLongitude() != null)
            .map(seller -> {
                double distance = calculateDistance(customerLat, customerLng, 
                    seller.getLatitude(), seller.getLongitude());
                return new SellerDistanceInfo(seller, distance);
            })
            .sorted((a, b) -> Double.compare(a.getDistance(), b.getDistance()))
            .collect(Collectors.toList());
    }
    
    // Inner class for seller with distance information
    public static class SellerDistanceInfo {
        private User seller;
        private double distance;
        
        public SellerDistanceInfo(User seller, double distance) {
            this.seller = seller;
            this.distance = distance;
        }
        
        public User getSeller() { return seller; }
        public double getDistance() { return distance; }
        public String getFormattedDistance() {
            if (distance < 1) {
                return String.format("%.0f meters", distance * 1000);
            }
            return String.format("%.1f km", distance);
        }
    }
}
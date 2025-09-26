package com.secondhand.store.service;

import com.secondhand.store.entity.User;
import com.secondhand.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {



    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ USER REGISTRATION & AUTHENTICATION ============

    /**
     * Register a new user
     */
    public User registerUser(User user) throws Exception {
        // Validate that username and email don't already exist
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new Exception("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default values
        user.setRole(User.Role.CUSTOMER);
        user.setIsActive(true);
        user.setSellerRating(0.0);
        user.setTotalSales(0);

        // Save using injected repository
        return userRepository.save(user);
    }

    /**
     * Authenticate user by username and password
     */
    public Optional<User> authenticateUser(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    // ============ USER MANAGEMENT ============

    public List<User> getAllUsers() {
        return userRepository.findAll();  // <-- Using injected repository
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);  // <-- Custom repository method
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long userId, User updatedUser) throws Exception {
        Optional<User> existingUserOpt = userRepository.findById(userId);

        if (existingUserOpt.isEmpty()) {
            throw new Exception("User not found with ID: " + userId);
        }

        User existingUser = existingUserOpt.get();

        // Update fields (don't update username, email, or password here)
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setAddress(updatedUser.getAddress());

        return userRepository.save(existingUser);
    }

    public void deactivateUser(Long userId) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new Exception("User not found with ID: " + userId);
        }

        User user = userOpt.get();
        user.setIsActive(false);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) throws Exception {
        if (!userRepository.existsById(userId)) {
            throw new Exception("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    // ============ SELLER-SPECIFIC METHODS ============

    public List<User> getAllSellers() {
        return userRepository.findByRole(User.Role.CUSTOMER); // In our system, customers can be sellers
    }

    public List<User> getSellersWithProducts() {
        return userRepository.findSellersWithAvailableProducts();
    }

    public List<User> getTopRatedSellers(Double minRating) {
        return userRepository.findBySellerRatingGreaterThan(minRating);
    }

    public void updateSellerRating(Long sellerId, Double newRating) throws Exception {
        Optional<User> sellerOpt = userRepository.findById(sellerId);

        if (sellerOpt.isEmpty()) {
            throw new Exception("Seller not found with ID: " + sellerId);
        }

        User seller = sellerOpt.get();
        seller.setSellerRating(newRating);
        userRepository.save(seller);
    }

    public void incrementSellerSales(Long sellerId) throws Exception {
        Optional<User> sellerOpt = userRepository.findById(sellerId);

        if (sellerOpt.isEmpty()) {
            throw new Exception("Seller not found with ID: " + sellerId);
        }

        User seller = sellerOpt.get();
        seller.setTotalSales(seller.getTotalSales() + 1);
        userRepository.save(seller);
    }

    // ============ SEARCH & ANALYTICS ============

    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.searchUsersByName(searchTerm);
    }

    public long getActiveUserCountByRole(User.Role role) {
        return userRepository.countActiveUsersByRole(role);
    }

    public List<User> getUsersWithOrders() {
        return userRepository.findUsersWithOrders();
    }

    // ============ VALIDATION HELPERS ============

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public void validateUserRegistration(User user) throws Exception {
        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            throw new Exception("Username must be at least 3 characters long");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new Exception("Password must be at least 6 characters long");
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new Exception("Valid email is required");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new Exception("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new Exception("Last name is required");
        }
    }
}
package com.secondhand.store.repository;

import com.secondhand.store.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically implements these methods based on method names!


    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);


    Optional<User> findByUsernameOrEmail(String username, String email);


    boolean existsByUsername(String username);


    boolean existsByEmail(String email);


    List<User> findByIsActiveTrue();


    List<User> findByRole(User.Role role);


    List<User> findByFirstNameContainingIgnoreCase(String firstName);


    List<User> findBySellerRatingGreaterThan(Double rating);


    List<User> findTop10ByRoleOrderByTotalSalesDesc(User.Role role);

    // Custom queries using @Query annotation


    @Query("SELECT DISTINCT u FROM User u JOIN u.productsForSale p WHERE p.isAvailable = true")
    List<User> findSellersWithAvailableProducts();

    /**
     * Search users by name (first name or last name)
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsersByName(@Param("searchTerm") String searchTerm);

    /**
     * Get user statistics
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") User.Role role);

    /**
     * Find users who have made purchases
     */
    @Query("SELECT DISTINCT u FROM User u WHERE EXISTS (SELECT o FROM Order o WHERE o.buyer = u)")
    List<User> findUsersWithOrders();

    /**
     * Update user's seller rating
     * This is a custom update query
     */
    @Query("UPDATE User u SET u.sellerRating = :rating WHERE u.id = :userId")
    void updateSellerRating(@Param("userId") Long userId, @Param("rating") Double rating);
}
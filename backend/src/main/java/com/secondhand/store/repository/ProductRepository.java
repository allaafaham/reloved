package com.secondhand.store.repository;

import com.secondhand.store.entity.Product;
import com.secondhand.store.entity.Category;
import com.secondhand.store.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic product queries

    /**
     * Find all available products
     */
    List<Product> findByIsAvailableTrue();

    /**
     * Find available products with pagination
     */
    Page<Product> findByIsAvailableTrue(Pageable pageable);

    /**
     * Find products by category
     */
    List<Product> findByCategoryAndIsAvailableTrue(Category category);

    /**
     * Find products by seller
     */
    List<Product> findBySellerAndIsAvailableTrue(User seller);

    /**
     * Find products by condition
     */
    List<Product> findByConditionAndIsAvailableTrue(Product.ProductCondition condition);

    // Price-based queries

    /**
     * Find products within price range
     */
    List<Product> findByIsAvailableTrueAndPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find products under a certain price
     */
    List<Product> findByIsAvailableTrueAndPriceLessThanEqual(BigDecimal maxPrice);

    // Search queries

    /**
     * Search products by name (case-insensitive)
     */
    List<Product> findByNameContainingIgnoreCaseAndIsAvailableTrue(String name);

    /**
     * Search by brand
     */
    List<Product> findByBrandContainingIgnoreCaseAndIsAvailableTrue(String brand);

    /**
     * Find products by location
     */
    List<Product> findByLocationCityIgnoreCaseAndIsAvailableTrue(String city);

    /**
     * Find negotiable products
     */
    List<Product> findByNegotiableTrueAndIsAvailableTrue();

    // Sorting and ordering

    /**
     * Find latest products (most recent first)
     */
    List<Product> findTop20ByIsAvailableTrueOrderByCreatedAtDesc();

    /**
     * Find most viewed products
     */
    List<Product> findTop10ByIsAvailableTrueOrderByViewCountDesc();

    /**
     * Find cheapest products first
     */
    List<Product> findByIsAvailableTrueOrderByPriceAsc();

    // Complex custom queries

    /**
     * Search products by multiple criteria
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isAvailable = true AND " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:condition IS NULL OR p.condition = :condition) AND " +
            "(:city IS NULL OR LOWER(p.locationCity) = LOWER(:city))")
    Page<Product> searchProducts(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("condition") Product.ProductCondition condition,
            @Param("city") String city,
            Pageable pageable);

    /**
     * Find similar products (same category, similar price range)
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.id != :productId AND " +
            "p.isAvailable = true AND " +
            "p.category = :category AND " +
            "p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findSimilarProducts(
            @Param("productId") Long productId,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Get products statistics by category
     */
    @Query("SELECT p.category, COUNT(p), AVG(p.price) FROM Product p " +
            "WHERE p.isAvailable = true GROUP BY p.category")
    List<Object[]> getProductStatsByCategory();

    /**
     * Find products by seller with high rating
     */
    @Query("SELECT p FROM Product p JOIN p.seller s WHERE " +
            "p.isAvailable = true AND s.sellerRating >= :minRating")
    List<Product> findProductsByHighRatedSellers(@Param("minRating") Double minRating);

    /**
     * Full text search in name, description, and keywords
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.isAvailable = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Analytics queries

    /**
     * Count products by condition
     */
    @Query("SELECT p.condition, COUNT(p) FROM Product p WHERE p.isAvailable = true GROUP BY p.condition")
    List<Object[]> countProductsByCondition();

    /**
     * Get average price by category
     */
    @Query("SELECT c.name, AVG(p.price) FROM Product p JOIN p.category c " +
            "WHERE p.isAvailable = true GROUP BY c.name")
    List<Object[]> getAveragePriceByCategory();
}
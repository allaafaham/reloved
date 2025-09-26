package com.secondhand.store.service;

import com.secondhand.store.entity.Product;
import com.secondhand.store.entity.Category;
import com.secondhand.store.entity.User;
import com.secondhand.store.repository.ProductRepository;
import com.secondhand.store.repository.CategoryRepository;
import com.secondhand.store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ============ PRODUCT MANAGEMENT ============

    public Product createProduct(Product product, Long categoryId, Long sellerId) throws Exception {
        // Validate and set category
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new Exception("Category not found with ID: " + categoryId);
        }
        product.setCategory(categoryOpt.get());

        // Validate and set seller
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new Exception("Seller not found with ID: " + sellerId);
        }
        product.setSeller(sellerOpt.get());

        // Set default values
        product.setIsAvailable(true);
        product.setIsSold(false);
        product.setViewCount(0L);
        product.setFavoriteCount(0L);

        // Validate product data
        validateProduct(product);

        return productRepository.save(product);
    }


    public List<Product> getAllAvailableProducts() {
        return productRepository.findByIsAvailableTrue();
    }


    public Page<Product> getAvailableProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByIsAvailableTrue(pageable);
    }


    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByIdAndIncrementView(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.incrementViewCount(); // Helper method we created in entity
            productRepository.save(product);
            return Optional.of(product);
        }

        return Optional.empty();
    }


    public Product updateProduct(Long productId, Product updatedProduct) throws Exception {
        Optional<Product> existingProductOpt = productRepository.findById(productId);

        if (existingProductOpt.isEmpty()) {
            throw new Exception("Product not found with ID: " + productId);
        }

        Product existingProduct = existingProductOpt.get();

        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setCondition(updatedProduct.getCondition());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setModel(updatedProduct.getModel());
        existingProduct.setColor(updatedProduct.getColor());
        existingProduct.setSize(updatedProduct.getSize());
        existingProduct.setLocationCity(updatedProduct.getLocationCity());
        existingProduct.setLocationState(updatedProduct.getLocationState());
        existingProduct.setNegotiable(updatedProduct.getNegotiable());
        existingProduct.setKeywords(updatedProduct.getKeywords());

        validateProduct(existingProduct);

        return productRepository.save(existingProduct);
    }


    public void deleteProduct(Long productId) throws Exception {
        if (!productRepository.existsById(productId)) {
            throw new Exception("Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }


    public void markProductAsSold(Long productId) throws Exception {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            throw new Exception("Product not found with ID: " + productId);
        }

        Product product = productOpt.get();
        product.markAsSold(); // Helper method from entity
        productRepository.save(product);
    }

    // ============ PRODUCT SEARCH & FILTERING ============


    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrue(name);
    }

    public Page<Product> searchProducts(String name, Long categoryId, BigDecimal minPrice,
                                        BigDecimal maxPrice, Product.ProductCondition condition,
                                        String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return productRepository.searchProducts(name, categoryId, minPrice, maxPrice,
                condition, city, pageable);
    }


    public Page<Product> fullTextSearch(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.fullTextSearch(searchTerm, pageable);
    }

    public List<Product> getProductsByCategory(Long categoryId) throws Exception {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new Exception("Category not found with ID: " + categoryId);
        }

        return productRepository.findByCategoryAndIsAvailableTrue(categoryOpt.get());
    }


    public List<Product> getProductsBySeller(Long sellerId) throws Exception {
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new Exception("Seller not found with ID: " + sellerId);
        }

        return productRepository.findBySellerAndIsAvailableTrue(sellerOpt.get());
    }


    public List<Product> getProductsByCondition(Product.ProductCondition condition) {
        return productRepository.findByConditionAndIsAvailableTrue(condition);
    }


    public List<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByIsAvailableTrueAndPriceBetween(minPrice, maxPrice);
    }


    public List<Product> getLatestProducts(int limit) {
        return productRepository.findTop20ByIsAvailableTrueOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }


    public List<Product> getMostViewedProducts() {
        return productRepository.findTop10ByIsAvailableTrueOrderByViewCountDesc();
    }


    public List<Product> getSimilarProducts(Long productId, int limit) throws Exception {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            throw new Exception("Product not found with ID: " + productId);
        }

        Product product = productOpt.get();
        BigDecimal price = product.getPrice();
        BigDecimal minPrice = price.multiply(BigDecimal.valueOf(0.7)); // 30% lower
        BigDecimal maxPrice = price.multiply(BigDecimal.valueOf(1.3)); // 30% higher

        Pageable pageable = PageRequest.of(0, limit);

        return productRepository.findSimilarProducts(productId, product.getCategory(),
                minPrice, maxPrice, pageable);
    }

    // ============ PRODUCT ANALYTICS ============


    public List<Object[]> getProductStatsByCategory() {
        return productRepository.getProductStatsByCategory();
    }


    public List<Object[]> countProductsByCondition() {
        return productRepository.countProductsByCondition();
    }


    public List<Object[]> getAveragePriceByCategory() {
        return productRepository.getAveragePriceByCategory();
    }

    // ============ VALIDATION ============


    private void validateProduct(Product product) throws Exception {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new Exception("Product name is required");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Product price must be greater than 0");
        }

        if (product.getCondition() == null) {
            throw new Exception("Product condition is required");
        }

        if (product.getCategory() == null) {
            throw new Exception("Product category is required");
        }

        if (product.getSeller() == null) {
            throw new Exception("Product seller is required");
        }
    }
}
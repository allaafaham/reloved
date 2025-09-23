package com.secondhand.store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    // Store product details at time of order
    // In case product details change or get deleted later
    @NotNull(message = "Price is required")
    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrder;

    @Column(name = "product_name_at_order")
    private String productNameAtOrder;

    @Column(name = "product_condition_at_order")
    @Enumerated(EnumType.STRING)
    private Product.ProductCondition productConditionAtOrder;

    // Seller information at time of order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(name = "seller_name_at_order")
    private String sellerNameAtOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtOrder = product.getPrice();
        this.productNameAtOrder = product.getName();
        this.productConditionAtOrder = product.getCondition();
        this.seller = product.getSeller();
        if (seller != null) {
            this.sellerNameAtOrder = seller.getFirstName() + " " + seller.getLastName();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtOrder() {
        return priceAtOrder;
    }

    public void setPriceAtOrder(BigDecimal priceAtOrder) {
        this.priceAtOrder = priceAtOrder;
    }

    public String getProductNameAtOrder() {
        return productNameAtOrder;
    }

    public void setProductNameAtOrder(String productNameAtOrder) {
        this.productNameAtOrder = productNameAtOrder;
    }

    public Product.ProductCondition getProductConditionAtOrder() {
        return productConditionAtOrder;
    }

    public void setProductConditionAtOrder(Product.ProductCondition productConditionAtOrder) {
        this.productConditionAtOrder = productConditionAtOrder;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getSellerNameAtOrder() {
        return sellerNameAtOrder;
    }

    public void setSellerNameAtOrder(String sellerNameAtOrder) {
        this.sellerNameAtOrder = sellerNameAtOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public BigDecimal getTotalPrice() {
        return priceAtOrder.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDisplayName() {
        return productNameAtOrder != null ? productNameAtOrder :
                (product != null ? product.getName() : "Unknown Product");
    }

    public String getDisplayCondition() {
        return productConditionAtOrder != null ? productConditionAtOrder.getDisplayName() :
                (product != null ? product.getCondition().getDisplayName() : "Unknown");
    }
}
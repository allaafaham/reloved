package com.secondhand.store.repository;

import com.secondhand.store.entity.Order;
import com.secondhand.store.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByBuyer(User buyer);

    Page<Order> findByBuyer(User buyer, Pageable pageable);

    List<Order> findByOrderStatus(Order.OrderStatus status);

    List<Order> findByPaymentStatus(Order.PaymentStatus status);

    @Query("SELECT o FROM Order o WHERE o.buyer = :buyer ORDER BY o.createdAt DESC")
    List<Order> findUserOrdersOrderByDateDesc(@Param("buyer") User buyer);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
            "o.orderStatus = 'DELIVERED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find orders containing products from a specific seller
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller = :seller")
    List<Order> findOrdersBySeller(@Param("seller") User seller);
}
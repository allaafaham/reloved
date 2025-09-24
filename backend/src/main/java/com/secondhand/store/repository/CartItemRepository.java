package com.secondhand.store.repository;

import com.secondhand.store.entity.CartItem;
import com.secondhand.store.entity.User;
import com.secondhand.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user); // Clear cart

    void deleteByUserAndProduct(User user, Product product);

    @Query("SELECT SUM(c.priceAtTime * c.quantity) FROM CartItem c WHERE c.user = :user")
    BigDecimal calculateCartTotal(@Param("user") User user);

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user = :user")
    int countItemsInCart(@Param("user") User user);
}

package com.ucop.entity;

import com.ucop.entity.enums.OrderStatus;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
public class Cart extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CART;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Account getCustomer() {
        return customer;
    }

    public void setCustomer(Account customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Set<CartItem> getItems() {
        if (items == null) {
            items = new HashSet<>();
        }
        return items;
    }

    public void setItems(Set<CartItem> items) {
        this.items = (items != null) ? items : new HashSet<>();
    }
}

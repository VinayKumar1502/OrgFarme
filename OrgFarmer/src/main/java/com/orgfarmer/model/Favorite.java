package com.orgfarmer.model;

import javax.persistence.*;

@Entity
@Table(name = "favorites")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    private boolean active = true;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
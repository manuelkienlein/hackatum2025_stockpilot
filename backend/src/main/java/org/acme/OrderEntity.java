package org.acme;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.util.Date;

@Entity
@Cacheable
public class OrderEntity extends PanacheEntity {
    public Long stock_id;
    public int quantity;
    public Date placed_at;
    public Float price_per_unit;

    public OrderEntity() {
    }
}

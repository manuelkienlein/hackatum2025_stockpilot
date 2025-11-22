package de.tum.hack.stockpilot.entities;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class StockEntity extends PanacheEntity {
    @Column(nullable = false, unique = true)
    public String symbol;
    public String name;
    public String exchange;
    @Column(nullable = false, unique = true)
    public String isin;

    public StockEntity() {
    }
}

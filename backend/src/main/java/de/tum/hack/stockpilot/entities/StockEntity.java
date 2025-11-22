package de.tum.hack.stockpilot.entities;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class StockEntity extends PanacheEntity {
    public String symbol;
    public String name;
    public String exchange;
    public String isin;

    public StockEntity() {
    }
}

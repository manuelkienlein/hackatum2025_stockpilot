package org.acme;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class StockEntity extends PanacheEntity {
    private Long id;
    private String symbol;
    private String name;
    private String exchange;
    private String isin;

    public StockEntity() {
    }
}

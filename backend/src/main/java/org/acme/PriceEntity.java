package org.acme;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class PriceEntity extends PanacheEntity {
    public Long id;
    public Long stock_id;
    public Float open;
    public Float close;
    public Float high;
    public Float low;
    public Long volume;

    public PriceEntity() {
    }
}

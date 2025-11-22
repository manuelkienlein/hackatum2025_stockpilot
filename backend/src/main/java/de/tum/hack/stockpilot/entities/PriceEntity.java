package de.tum.hack.stockpilot.entities;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import java.util.Date;

@Entity
@Cacheable
public class PriceEntity extends PanacheEntity {
    public String symbol;
    public Date date;
    public Float open;
    public Float close;
    public Float high;
    public Float low;
    public Long volume;

    public PriceEntity() {
    }

    public static Float findLatestClosePrice(String symbol) {
        PriceEntity result = find("symbol = ?1 ORDER BY date DESC", symbol)
                .firstResult();

        return result != null ? result.close : null;
    }
}

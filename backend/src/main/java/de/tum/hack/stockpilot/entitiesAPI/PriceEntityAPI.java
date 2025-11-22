package de.tum.hack.stockpilot.entitiesAPI;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.util.Date;

@Entity
public class PriceEntityAPI extends PanacheEntity {
    public String symbol;
    public Date date;
    public Float open;
    public Float high;
    public Float low;
    public Float close;
    public Long volume;
    public Float charge;
    public Float changePercent;
    public Float vwap;

    public PriceEntityAPI() {
    }
}

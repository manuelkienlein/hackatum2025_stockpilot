package de.tum.hack.stockpilot.entitiesAPI;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.util.Date;

@Entity
public class StockEntityAPI extends PanacheEntity {
    public String symbol;
    public String companyName;
    public String exchange;
    public String isin;

    public StockEntityAPI() {
    }
}

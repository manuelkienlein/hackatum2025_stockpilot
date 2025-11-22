package de.tum.hack.stockpilot.dto;

import de.tum.hack.stockpilot.entities.StockEntity;

import java.util.Date;

public class WatchlistItemDTO {
    public Long id;
    public StockEntity stock;
    public Float initialPrice;
    public Float currentPrice;
    public Date createdAt;
}

package de.tum.hack.stockpilot.dto;

import java.math.BigDecimal;

public class CreateWatchlistEntryRequest {
    public String symbol;           // Stocksymbol, z.B. "AAPL"
    public BigDecimal priceAtCreation;
}
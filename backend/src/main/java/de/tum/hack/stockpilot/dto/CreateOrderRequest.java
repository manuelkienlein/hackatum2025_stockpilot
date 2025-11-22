package de.tum.hack.stockpilot.dto;

import java.math.BigDecimal;

public class CreateOrderRequest {

    public String symbol;
    public String side;   // "BUY" / "SELL"
    public String type;   // "MARKET", "LIMIT", "STOP", "STOP_LIMIT"
    public int quantity;
    public BigDecimal limitPrice;
    public BigDecimal stopPrice;
}

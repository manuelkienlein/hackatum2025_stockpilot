package de.tum.hack.stockpilot.dto;

import java.math.BigDecimal;

public class ExecuteOrderRequest {
    public BigDecimal executedPrice;
    public Integer executedQuantity; // optional, wenn null => komplette Menge
}
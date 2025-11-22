package de.tum.hack.stockpilot.dto;

import java.util.Date;

public class PriceEntityResponse {
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

    public PriceEntityResponse() {
    }
}

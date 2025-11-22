package de.tum.hack.stockpilot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

// kann später in Order + Trade aufgesplittet werden
@Entity
@Table(name = "orders")
public class OrderEntity extends PanacheEntity {

    public enum OrderType {
        MARKET,
        LIMIT,
        STOP,
        STOP_LIMIT
    }

    public enum OrderStatus {
        PENDING,
        EXECUTED,
        CANCELLED
    }

    public enum OrderSide {
        BUY,
        SELL
    }

    // ⚠️ id kommt von PanacheEntity: public Long id;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;    // BUY oder SELL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;    // MARKET, LIMIT, STOP, STOP_LIMIT

    @Column(nullable = false)
    private int quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal limitPrice;  // bei LIMIT & STOP_LIMIT Pflicht, sonst null

    @Column(precision = 19, scale = 4)
    private BigDecimal stopPrice;   // bei STOP & STOP_LIMIT Pflicht, sonst null

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal executedPrice;  // effektiver Ausführungspreis

    @Column
    private Instant executedAt;        // Zeitpunkt der Ausführung

    @Column
    private Integer executedQuantity;  // i.d.R. = quantity, wenn voll ausgeführt


    // --- Lifecycle Hooks ---

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }


    // --- Konstruktoren ---

    public OrderEntity() {
        // JPA / Hibernate braucht den
    }

    public OrderEntity(
            String symbol,
            OrderSide side,
            OrderType type,
            int quantity,
            BigDecimal limitPrice,
            BigDecimal stopPrice
    ) {
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.stopPrice = stopPrice;
        // createdAt & status werden in @PrePersist gesetzt
    }


    // --- Domain-Methoden ---

    /**
     * Markiert die Order als voll ausgeführt.
     */
    public void execute(BigDecimal price) {
        execute(price, this.quantity);
    }

    /**
     * Markiert die Order als (voll oder teilweise) ausgeführt.
     */
    public void execute(BigDecimal price, int executedQuantity) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled orders cannot be executed");
        }
        if (executedQuantity <= 0 || executedQuantity > this.quantity) {
            throw new IllegalArgumentException("Invalid executed quantity: " + executedQuantity);
        }

        this.executedPrice = price;
        this.executedQuantity = executedQuantity;
        this.executedAt = Instant.now();
        this.status = OrderStatus.EXECUTED; // für echtes Partial-Fill später ggf. PARTIALLY_EXECUTED etc.
    }

    /**
     * Bricht die Order ab, sofern sie noch nicht ausgeführt wurde.
     */
    public void cancel() {
        if (status == OrderStatus.EXECUTED) {
            throw new IllegalStateException("Executed orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isOpen() {
        return status == OrderStatus.PENDING;
    }

    public boolean isExecuted() {
        return status == OrderStatus.EXECUTED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }


    // --- Getter/Setter ---

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public void setExecutedPrice(BigDecimal executedPrice) {
        this.executedPrice = executedPrice;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public Integer getExecutedQuantity() {
        return executedQuantity;
    }

    public void setExecutedQuantity(Integer executedQuantity) {
        this.executedQuantity = executedQuantity;
    }
}

package de.tum.hack.stockpilot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "watchlist",
        uniqueConstraints = {
                // Optional: Pro Stock nur ein Watchlist-Eintrag
                @UniqueConstraint(columnNames = {"stock_id"})
        }
)
public class WatchlistEntity extends PanacheEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    public StockEntity stock;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal priceAtCreation;

    @Column(nullable = false, updatable = false)
    public Instant createdAt;

    public WatchlistEntity() {
    }

    public WatchlistEntity(StockEntity stock, BigDecimal priceAtCreation) {
        this.stock = stock;
        this.priceAtCreation = priceAtCreation;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Prüfen, ob ein Stock schon auf der Watchlist ist.
     */
    public static boolean existsForSymbol(String symbol) {
        return count("stock.symbol", symbol) > 0;
    }
}

package de.tum.hack.stockpilot.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "networth_snapshot")
public class NetWorthSnapshotEntity extends PanacheEntity {

    @Column(name = "date", nullable = false)
    public LocalDate date;

    // optional: wenn du zwischen mehreren Depots/Accounts unterscheiden willst
    // @ManyToOne
    // @JoinColumn(name = "account_id")
    // public AccountEntity account;

    @Column(name = "total_value", nullable = false)
    public BigDecimal totalValue;

    @Column(name = "cash_value", nullable = false)
    public BigDecimal cashValue;

    @Column(name = "invested_value", nullable = false)
    public BigDecimal investedValue;

}

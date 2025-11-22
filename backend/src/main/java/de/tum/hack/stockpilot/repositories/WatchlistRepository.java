package de.tum.hack.stockpilot.repositories;

import de.tum.hack.stockpilot.entities.WatchlistEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WatchlistRepository implements PanacheRepository<WatchlistEntity> {

    public List<WatchlistEntity> listAllSorted() {
        return listAll(Sort.descending("createdAt"));
    }

    public List<WatchlistEntity> findBySymbolSorted(String symbol) {
        return find("stock.symbol", Sort.descending("createdAt"), symbol).list();
    }
}
package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.dto.PortfolioRequest;
import de.tum.hack.stockpilot.entities.OrderEntity;
import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entities.StockEntity;
import de.tum.hack.stockpilot.repositories.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;

@Path("/portfolio")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class PortfolioResource {

    @Inject
    OrderRepository orderRepository;

    @GET
    public List<PortfolioRequest> get() {
        List<PortfolioRequest> portfolio = new ArrayList<>();

        List<OrderEntity> orders = OrderEntity.listAll();

        // Sum up quantity
        for (OrderEntity order : orders) {

            PortfolioRequest portfolioEntry = null;

            for (PortfolioRequest entry : portfolio) {
                if (entry.symbol.equals(order.getSymbol())) {
                    portfolioEntry = entry;
                }
            }

            if (portfolioEntry == null) {
                portfolioEntry = new PortfolioRequest();
                portfolioEntry.symbol = order.getSymbol();

                portfolio.add(portfolioEntry);
            }

            int diff = order.getSide() == OrderEntity.OrderSide.BUY ? order.getQuantity() : -order.getQuantity();

            portfolioEntry.quantity += diff;
            portfolioEntry.total_spend_for_stocks += order.getExecutedPrice().toBigInteger().floatValue() * diff;
        }

        for (PortfolioRequest portfolioEntry : portfolio) {

            // get stock data to set name
            StockEntity stock = StockEntity.find("symbol", portfolioEntry.symbol).firstResult();

            if (stock == null) {
                System.out.println("Could not find stock data for " + portfolioEntry.symbol);
                continue;
            }
            portfolioEntry.name = stock.name;

            // get value and calculate current value
            Optional<PriceEntity> latestPrice = PriceEntity.find("stock_id", stock.id)
                    .stream().map(e -> (PriceEntity) e).max(Comparator.comparing(PriceEntity::getDate));

            if (latestPrice.isEmpty()) {
                System.out.println("Could not find price data for " + portfolioEntry.symbol);
                continue;
            }

            portfolioEntry.current = latestPrice.get().close;
            portfolioEntry.value = portfolioEntry.quantity * portfolioEntry.current;

            portfolioEntry.avg_buy = portfolioEntry.total_spend_for_stocks / portfolioEntry.quantity;

            portfolioEntry.profit_absolut = portfolioEntry.value - portfolioEntry.total_spend_for_stocks;
            portfolioEntry.profit_percent = portfolioEntry.value / portfolioEntry.total_spend_for_stocks - 1;
        }


        return portfolio;
    }
}
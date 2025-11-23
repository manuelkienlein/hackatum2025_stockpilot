package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.dto.PortfolioResponse;
import de.tum.hack.stockpilot.entities.OrderEntity;
import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entities.StockEntity;
import de.tum.hack.stockpilot.repositories.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Path("/portfolio")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class PortfolioResource {

    @Inject
    OrderRepository orderRepository;

    @GET
    public List<PortfolioResponse> get() {
        List<PortfolioResponse> portfolio = new ArrayList<>();

        List<OrderEntity> orders = OrderEntity.listAll();

        // Sum up quantity
        for (OrderEntity order : orders) {

            if(!order.isExecuted()) {
                continue;
            }

            PortfolioResponse portfolioEntry = null;

            for (PortfolioResponse entry : portfolio) {
                if (entry.symbol.equals(order.getSymbol())) {
                    portfolioEntry = entry;
                }
            }

            if (portfolioEntry == null) {
                portfolioEntry = new PortfolioResponse();
                portfolioEntry.symbol = order.getSymbol();

                portfolio.add(portfolioEntry);
            }

            int diff = order.getSide() == OrderEntity.OrderSide.BUY ? order.getQuantity() : -order.getQuantity();

            portfolioEntry.quantity += diff;
            portfolioEntry.total_spend_for_stocks += order.getExecutedPrice().toBigInteger().floatValue() * diff;
        }

        for (PortfolioResponse portfolioEntry : portfolio) {

            // get stock data to set name
            StockEntity stock = StockEntity.find("symbol", portfolioEntry.symbol).firstResult();

            if (stock == null) {
                System.out.println("Could not find stock data for " + portfolioEntry.symbol);
                continue;
            }
            portfolioEntry.name = stock.name;
            portfolioEntry.exchange = stock.exchange;

            // get value and calculate current value
            Optional<PriceEntity> latestPrice = PriceEntity.find("symbol", stock.symbol)
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
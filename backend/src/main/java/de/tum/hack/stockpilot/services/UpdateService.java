package de.tum.hack.stockpilot.services;

import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entities.StockEntity;
import de.tum.hack.stockpilot.entitiesAPI.PriceEntityAPI;
import de.tum.hack.stockpilot.entitiesAPI.StockEntityAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class UpdateService {

    @Inject
    @RestClient
    ExternalApiClient apiClient;

    @ConfigProperty(name = "external-api.apikey")
    String apiKey;

    @Transactional
    public void fetchPriceHistory(String symbol) {

        StockEntity stock = StockEntity.find("symbol", symbol).firstResult();

        if (stock == null) {
            System.err.println("Warning: Skipping stock with symbol \"" + symbol + "\"");
            return;
        }

        List<PriceEntityAPI> users = apiClient.getPricesFromAPI(symbol, apiKey);

        for (PriceEntityAPI entityAPI : users) {
            PriceEntity price = new PriceEntity();
            price.stock_id = stock.id;
            price.date = entityAPI.date;
            price.open = entityAPI.open;
            price.close = entityAPI.close;
            price.high = entityAPI.high;
            price.low = entityAPI.low;
            price.volume = entityAPI.volume;
            price.persist();
        }
    }

    @Transactional
    public void fetchStock(String symbol) {
        StockEntity stock = StockEntity.find("symbol", symbol).firstResult();
        if (stock != null) {
            return;
        }

        stock = new StockEntity();

        StockEntityAPI stockAPI = apiClient.getStockInfoFromAPI(symbol, apiKey).get(0);

        stock.symbol = stockAPI.symbol;
        stock.name = stockAPI.companyName;
        stock.exchange = stockAPI.exchange;
        stock.isin = stockAPI.isin;
        stock.persist();
    }
}


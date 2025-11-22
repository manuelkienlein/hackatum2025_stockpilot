package de.tum.hack.stockpilot.services;

import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entitiesAPI.PriceEntityAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class UpdateService {

    @Inject
    @RestClient
    ExternalApiClient apiClient;

    @Transactional
    public void fetchPriceHistory(String symbol) {
        List<PriceEntityAPI> users = apiClient.getPricesFromAPI(symbol);

        for (PriceEntityAPI entityAPI : users) {
            PriceEntity price = new PriceEntity();
            price.stock_id = -1L;   // TODO
            price.date = entityAPI.date;
            price.open = entityAPI.open;
            price.close = entityAPI.close;
            price.high = entityAPI.high;
            price.low = entityAPI.low;
            price.volume = entityAPI.volume;
            price.persist();
        }
    }
}


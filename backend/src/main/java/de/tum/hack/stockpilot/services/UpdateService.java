package de.tum.hack.stockpilot.services;

import de.tum.hack.stockpilot.entities.PriceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UpdateService {

    //@Inject
    //@RestClient
    //ExternalApiClient apiClient;

    @Transactional
    public void fetchPriceHistory() {
        /*List<PriceEntity> users = apiClient.getPricesFromAPI("AAPL");

        for (PriceEntity dto : users) {
            PriceEntity price = new PriceEntity();
            price.open = 1.0f;
            price.close = 2.0f;
            price.persist();
        }*/
        PriceEntity price = new PriceEntity();
        price.open = 1.0f;
        price.close = 1.1f;
        price.persist();
    }
}


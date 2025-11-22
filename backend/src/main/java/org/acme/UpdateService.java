package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;

@ApplicationScoped
public class UpdateService {

    @Inject
    @RestClient
    //ExternalApiClient apiClient;

    @Transactional
    public void fetchAndSavePeople() {
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
        //price.persist();
    }
}


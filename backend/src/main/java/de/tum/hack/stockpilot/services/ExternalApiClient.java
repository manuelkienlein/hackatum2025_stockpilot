package de.tum.hack.stockpilot.services;

import de.tum.hack.stockpilot.entities.PriceEntity;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;

@Path("/historical-price-eod/full")
@RegisterRestClient(configKey="external-api")
public interface ExternalApiClient {
    @GET
    List<PriceEntity> getPricesFromAPI(@QueryParam("symbol") String symbol);
}

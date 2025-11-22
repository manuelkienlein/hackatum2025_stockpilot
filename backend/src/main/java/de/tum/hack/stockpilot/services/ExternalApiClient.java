package de.tum.hack.stockpilot.services;

import de.tum.hack.stockpilot.dto.PriceEntityResponse;
import de.tum.hack.stockpilot.dto.StockEntityResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;


@RegisterRestClient(configKey = "external-api")
public interface ExternalApiClient {
    @GET
    @Path("/historical-price-eod/full")
    List<PriceEntityResponse> getPricesFromAPI(@QueryParam("symbol") String symbol, @QueryParam("apikey") String apikey);

    @GET
    @Path("/profile")
    List<StockEntityResponse> getStockInfoFromAPI(@QueryParam("symbol") String symbol, @QueryParam("apikey") String apikey);
}

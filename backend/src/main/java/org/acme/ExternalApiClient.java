package org.acme;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;

@Path("/users")
@RegisterRestClient(configKey="external-api")
public interface ExternalApiClient {
    @GET
    List<PriceEntity> getPricesFromAPI();
}

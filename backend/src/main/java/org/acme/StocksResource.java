package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;

import io.quarkus.panache.common.Sort;

@Path("/stocks")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StocksResource {

    @GET
    public List<StockEntity> get() {
        return StockEntity.listAll(Sort.by("name"));
    }
}

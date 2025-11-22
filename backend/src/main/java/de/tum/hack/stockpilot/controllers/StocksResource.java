package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entities.StockEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.transaction.Transactional;

import java.util.List;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;

@Path("/stocks")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StocksResource {

    @GET
    public List<StockEntity> get() {
        return StockEntity.listAll(Sort.by("name"));
    }

    @GET
    @Path("{symbol}")
    public StockEntity getSingle(@PathParam("symbol") String symbol) {
        StockEntity entity = StockEntity.find("symbol", symbol).firstResult();
        if (entity == null) {
            throw new WebApplicationException("Stock with symbol " + symbol + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(StockEntity stock) {
        if (stock.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        stock.persist();
        return Response.ok(stock).status(201).build();
    }

    @DELETE
    @Path("{symbol}")
    @Transactional
    public Response delete(@PathParam("symbol") String symbol) {
        StockEntity entity = StockEntity.find("symbol", symbol).firstResult();
        if (entity == null) {
            throw new WebApplicationException("Stock with symbol " + symbol + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }

    @GET()
    @Path("{symbol}/history")
    public List<PriceEntity> get(@PathParam("symbol") String symbol) {
        return PriceEntity.find("symbol", Sort.by("date").ascending(), symbol).list();
    }
}

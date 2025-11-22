package de.tum.hack.stockpilot.controllers;

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
    @Path("{id}")
    public StockEntity getSingle(@PathParam("id") Long id) {
        StockEntity entity = StockEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Stock with id of " + id + " does not exist.", 404);
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
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        StockEntity entity = StockEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Stock with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
}

package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.entities.PriceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.transaction.Transactional;

import java.util.List;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;

@Path("/prices")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class PriceResource {

    @GET
    public List<PriceEntity> get() {
        return PriceEntity.listAll(Sort.by("date"));
    }

    @GET
    @Path("{id}")
    public PriceEntity getSingle(@PathParam("id") Long id) {
        PriceEntity entity = PriceEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Price listing with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(PriceEntity price) {
        if (price.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        price.persist();
        return Response.ok(price).status(201).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        PriceEntity entity = PriceEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Stock with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
}

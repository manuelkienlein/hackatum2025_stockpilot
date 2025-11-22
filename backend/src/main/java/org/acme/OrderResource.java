package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.transaction.Transactional;

import java.util.List;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class OrderResource {

    @GET
    public List<OrderEntity> get() {
        return OrderEntity.listAll(Sort.by("placed_at"));
    }

    @GET
    @Path("{id}")
    public OrderEntity getSingle(Long id) {
        OrderEntity entity = OrderEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Order with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    @Transactional
    public Response create(OrderEntity stock) {
        if (stock.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        stock.persist();
        return Response.ok(stock).status(201).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(Long id) {
        OrderEntity entity = OrderEntity.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Stock with id of " + id + " does not exist.", 404);
        }
        entity.delete();
        return Response.status(204).build();
    }
}


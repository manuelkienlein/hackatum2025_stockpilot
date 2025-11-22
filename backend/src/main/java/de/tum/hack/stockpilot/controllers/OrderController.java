package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.dto.CreateOrderRequest;
import de.tum.hack.stockpilot.dto.ExecuteOrderRequest;
import de.tum.hack.stockpilot.entities.OrderEntity;
import de.tum.hack.stockpilot.repositories.OrderRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderController {

    @Inject
    OrderRepository orderRepository;

    // GET /orders -> alle Orders
    @GET
    public List<OrderEntity> listAll() {
        return orderRepository.listAll();
    }

    // GET /orders/{id} -> einzelne Order
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Optional<OrderEntity> orderOpt = orderRepository.findByIdOptional(id);
        if (orderOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(orderOpt.get()).build();
    }

    // POST /orders -> neue Order anlegen
    @POST
    @Transactional
    public Response createOrder(CreateOrderRequest request) {
        if (request.symbol == null || request.side == null || request.type == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("symbol, side und type sind Pflichtfelder")
                    .build();
        }

        OrderEntity.OrderSide side;
        OrderEntity.OrderType type;

        try {
            side = OrderEntity.OrderSide.valueOf(request.side.toUpperCase());
            type = OrderEntity.OrderType.valueOf(request.type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Ungültiger side oder type Wert")
                    .build();
        }

        OrderEntity order = new OrderEntity(
                request.symbol,
                side,
                type,
                request.quantity,
                request.limitPrice,
                request.stopPrice
        );

        orderRepository.persist(order);

        return Response
                .created(URI.create("/orders/" + order.id))
                .entity(order)
                .build();
    }

    // POST /orders/{id}/execute -> Order ausführen
    @POST
    @Path("/{id}/execute")
    @Transactional
    public Response executeOrder(@PathParam("id") Long id, ExecuteOrderRequest request) {
        Optional<OrderEntity> orderOpt = orderRepository.findByIdOptional(id);
        if (orderOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        OrderEntity order = orderOpt.get();

        if (request.executedPrice == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("executedPrice ist erforderlich")
                    .build();
        }

        int execQty = request.executedQuantity != null
                ? request.executedQuantity
                : order.getQuantity();

        try {
            order.execute(request.executedPrice, execQty);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }

        return Response.ok(order).build();
    }

    // POST /orders/{id}/cancel -> Order canceln
    @POST
    @Path("/{id}/cancel")
    @Transactional
    public Response cancelOrder(@PathParam("id") Long id) {
        Optional<OrderEntity> orderOpt = orderRepository.findByIdOptional(id);
        if (orderOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        OrderEntity order = orderOpt.get();
        try {
            order.cancel();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }

        return Response.ok(order).build();
    }

    // DELETE /orders/{id} -> hard delete (optional)
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteOrder(@PathParam("id") Long id) {
        boolean deleted = orderRepository.deleteById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}

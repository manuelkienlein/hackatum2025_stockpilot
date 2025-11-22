package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.dto.CreateWatchlistEntryRequest;
import de.tum.hack.stockpilot.dto.WatchlistItemDTO;
import de.tum.hack.stockpilot.entities.PriceEntity;
import de.tum.hack.stockpilot.entities.StockEntity;
import de.tum.hack.stockpilot.entities.WatchlistEntity;
import de.tum.hack.stockpilot.repositories.WatchlistRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Path("/watchlist")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WatchlistResource {

    @Inject
    WatchlistRepository watchlistRepository;

    // ---- READ ----

    @GET
    public List<WatchlistItemDTO> listAll() {
        // sortiert nach createdAt DESC
        return toDTOList(watchlistRepository.listAllSorted());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Optional<WatchlistEntity> entryOpt = watchlistRepository.findByIdOptional(id);
        if (entryOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(entryOpt.get()).build();
    }

    @GET
    @Path("/stock/{symbol}")
    public List<WatchlistEntity> findBySymbol(@PathParam("symbol") String symbol) {
        return watchlistRepository.findBySymbolSorted(symbol);
    }

    // ---- CREATE ----

    @POST
    @Transactional
    public Response create(CreateWatchlistEntryRequest request) {
        if (request.symbol == null || request.symbol.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("symbol ist erforderlich")
                    .build();
        }
        if (request.priceAtCreation == null) {
            PriceEntity result = PriceEntity.find("symbol = ?1 ORDER BY date DESC", request.symbol).firstResult();
            request.priceAtCreation = BigDecimal.valueOf(result != null ? result.close : null);
        }
        if (request.priceAtCreation == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("priceAtCreation ist erforderlich")
                    .build();
        }

        StockEntity stock = StockEntity.find("symbol", request.symbol).firstResult();
        if (stock == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Kein Stock mit Symbol: " + request.symbol)
                    .build();
        }

        WatchlistEntity entry = new WatchlistEntity(stock, request.priceAtCreation);
        entry.persist();

        return Response
                .created(URI.create("/watchlist/" + entry.id))
                .entity(entry)
                .build();
    }

    // ---- UPDATE (optional) ----
    // z.B. nur Preis nachträglich korrigieren

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, CreateWatchlistEntryRequest request) {
        Optional<WatchlistEntity> entryOpt = watchlistRepository.findByIdOptional(id);
        if (entryOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        WatchlistEntity entry = entryOpt.get();

        if (request.priceAtCreation != null) {
            entry.priceAtCreation = request.priceAtCreation;
        }
        if (request.symbol != null && !request.symbol.isBlank()) {
            StockEntity stock = StockEntity.find("symbol", request.symbol).firstResult();
            if (stock == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Kein Stock mit Symbol: " + request.symbol)
                        .build();
            }
            entry.stock = stock;
        }

        return Response.ok(entry).build();
    }

    // ---- DELETE ----

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = watchlistRepository.deleteById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }


    // Helper

    private static WatchlistItemDTO toDTO(WatchlistEntity w) {
        WatchlistItemDTO dto = new WatchlistItemDTO();
        dto.id = w.id;
        dto.stock = w.stock;
        dto.initialPrice = w.priceAtCreation.floatValue();
        dto.createdAt = Date.from(w.createdAt);

        // latestPrice holen
        dto.currentPrice = PriceEntity.findLatestClosePrice(w.stock.symbol);

        return dto;
    }

    private static List<WatchlistItemDTO> toDTOList(List<WatchlistEntity> list) {
        return list.stream().map(WatchlistResource::toDTO).toList();
    }
}
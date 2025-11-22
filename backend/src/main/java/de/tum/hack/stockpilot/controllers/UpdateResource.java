package de.tum.hack.stockpilot.controllers;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.inject.Inject;
import de.tum.hack.stockpilot.services.UpdateService;

@Path("/update")
public class UpdateResource {

    @Inject
    UpdateService service;

    @POST
    public String importData() {
        service.fetchPriceHistory("NVDA");
        return "Data imported";
    }
}

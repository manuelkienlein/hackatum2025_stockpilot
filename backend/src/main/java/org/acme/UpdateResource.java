package org.acme;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.inject.Inject;

@Path("/update")
public class UpdateResource {

    @Inject
    UpdateService service;

    @POST
    public String importData() {
        service.fetchAndSavePeople();
        return "Data imported";
    }
}

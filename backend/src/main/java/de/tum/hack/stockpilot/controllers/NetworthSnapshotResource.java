package de.tum.hack.stockpilot.controllers;

import de.tum.hack.stockpilot.entities.NetWorthSnapshotEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;

import java.util.List;

@Path("/networth")
@ApplicationScoped
@Produces("application/json")
public class NetworthSnapshotResource {

    @GET
    public List<NetWorthSnapshotEntity> getHistory() {
        // z.B. letzte 365 Tage
        return NetWorthSnapshotEntity.find("order by date asc").list();
    }
}

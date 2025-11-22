package de.tum.hack.stockpilot;

import de.tum.hack.stockpilot.config.AppConfig;
import de.tum.hack.stockpilot.services.UpdateService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class StockPilotApplication {


    private final AppConfig appConfig;

    @Inject
    UpdateService updateService;

    public StockPilotApplication(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    void onStart(@Observes StartupEvent ev) {
        System.out.println("🚀 App gestartet");
        System.out.println("📈 Init Stocks aus Config:");

        appConfig.init().stocks().forEach(
                symbol -> {
                    System.out.println(" - " + symbol);
                    updateService.fetchStock(symbol);
                    updateService.fetchPriceHistory(symbol);
                }
        );

    }
}

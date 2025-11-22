package de.tum.hack.stockpilot.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "app")
public interface AppConfig {

    Init init();

    interface Init {
        List<String> stocks();
    }
}

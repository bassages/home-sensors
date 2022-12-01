package nl.homesensors.homeserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "home-sensors.home-server.api")
record HomeServerApiConfig(
        String url,
        String basicAuthUser,
        String basicAuthPassword
) { }

package nl.homesensors.homeserver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@RequiredArgsConstructor
@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "home-sensors.home-server.api")
public class HomeServerApiConfig {

    private final String url;
    private final String basicAuthUser;
    private final String basicAuthPassword;
}

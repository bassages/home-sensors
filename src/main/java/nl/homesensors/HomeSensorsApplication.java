package nl.homesensors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.http.HttpClient;
import java.time.Clock;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableAsync
public class HomeSensorsApplication {

	public static void main(final String[] args) {
		SpringApplication.run(HomeSensorsApplication.class, args);
	}

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
	@Scope(value = SCOPE_PROTOTYPE)
	public HttpClient.Builder getHttpClientBuilder() {
		return HttpClient.newBuilder();
	}

	@Bean
	public Runtime getRuntime() {
		return Runtime.getRuntime();
	}
}

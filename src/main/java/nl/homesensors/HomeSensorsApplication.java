package nl.homesensors;

import nl.homesensors.homeserver.HomeServerApiConfig;
import nl.homesensors.sensortag.SensortagConfig;
import nl.homesensors.smartmeter.SmartMeterSerialPortConfiguration;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Clock;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@EnableConfigurationProperties({
		HomeServerApiConfig.class,
		SmartMeterSerialPortConfiguration.class,
		SensortagConfig.class
})
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
	public HttpClientBuilder getHttpClientBuilder() {
		return HttpClientBuilder.create();
	}

	@Bean
	public Runtime getRuntime() {
		return Runtime.getRuntime();
	}
}

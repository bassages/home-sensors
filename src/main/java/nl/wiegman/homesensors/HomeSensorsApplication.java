package nl.wiegman.homesensors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HomeSensorsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeSensorsApplication.class, args);
	}
}

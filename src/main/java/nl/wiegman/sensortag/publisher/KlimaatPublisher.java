package nl.wiegman.sensortag.publisher;

import java.math.BigDecimal;

public interface KlimaatPublisher {

    void publish(String klimaatSensorCode, BigDecimal temperatuur, BigDecimal luchtvochtigheid);
}

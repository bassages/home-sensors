package nl.wiegman.sensortag.publisher;

import java.math.BigDecimal;

public interface KlimaatPublisher {

    void publish(BigDecimal temperatuur, BigDecimal luchtvochtigheid);
}

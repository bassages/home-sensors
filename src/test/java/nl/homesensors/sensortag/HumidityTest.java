package nl.homesensors.sensortag;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class HumidityTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(Humidity.class).withNonnullFields("value").verify();
    }
}

package nl.homesensors.sensortag;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TemperatureTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(Temperature.class).verify();
    }
}

package nl.homesensors.sensortag;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SensorCodeTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(SensorCode.class).verify();
    }
}

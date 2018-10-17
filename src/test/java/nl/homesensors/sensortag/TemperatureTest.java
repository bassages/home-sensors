package nl.homesensors.sensortag;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TemperatureTest {

    @Test
    public void equals() {
        EqualsVerifier.forClass(Temperature.class).verify();
    }
}
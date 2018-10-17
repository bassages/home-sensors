package nl.homesensors.sensortag;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HumidityTest {

    @Test
    public void equals() {
        EqualsVerifier.forClass(Humidity.class).verify();
    }
}
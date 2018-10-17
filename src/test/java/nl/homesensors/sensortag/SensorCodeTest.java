package nl.homesensors.sensortag;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class SensorCodeTest {

    @Test
    public void equals() {
        EqualsVerifier.forClass(SensorCode.class).verify();
    }
}
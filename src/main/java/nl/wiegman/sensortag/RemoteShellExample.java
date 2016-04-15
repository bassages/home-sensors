package nl.wiegman.sensortag;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

public class RemoteShellExample {

    public static final String SENSORTAG_BLUETOOTH_ADDRESS = "BC:6A:29:AC:7D:31";

    public static void main(String[] args) throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession("pi", "192.168.178.25");
        session.setPassword("raspberry");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell)session.openChannel("shell");
        channel.connect();

        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
//                .withEchoOutput(System.err)
//                .withEchoInput(System.err)
                .withInputFilters(removeColors(), removeNonPrintable())
                .withExceptionOnFailure()
                .withTimeout(10, TimeUnit.SECONDS)
                .build();

        try {
            expect.expect(contains("~$"));

            connectToSensortag(expect);
            setConnectionParameters();


            /**
             * From the TI Sensortag Guide:
             *
             * The most power efficient way to obtain measurements for a sensor is to
             * 1. Enable notification
             * 2. Enable Sensor
             * 3. When notification with data is obtained at the Master side, disable the sensor (notification still on though)
             */

            enableNotifications(expect);

            int a = 1;
            while (a == 1) {
                readTemperature(expect);
                readHumidity(expect);

                Thread.sleep(50000);
            }

            disableNotifications(expect);

            expect.sendLine("disconnect");
            expect.sendLine("exit");
            expect.expect(contains("~$"));

        } finally {
            expect.close();
            channel.disconnect();
            session.disconnect();
        }
    }

    private static void setConnectionParameters() throws Exception {
        JSch jSch = new JSch();
        Session session = jSch.getSession("pi", "192.168.178.25");
        session.setPassword("raspberry");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        ChannelShell channel = (ChannelShell)session.openChannel("shell");
        channel.connect();

        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
//                .withEchoOutput(System.err)
//                .withEchoInput(System.err)
                .withInputFilters(removeColors(), removeNonPrintable())
                .withExceptionOnFailure()
                .withTimeout(10, TimeUnit.SECONDS)
                .build();

        try {
            expect.expect(contains("~$"));

            expect.sendLine("sudo hcitool con");
            Result result = expect.expect(regexp("handle (\\d+) state 1 lm MASTER"));
            String handle = result.group(1);

            expect.sendLine("sudo hcitool lecup --handle " + handle + " --min 200 --max 230");
            expect.expect(contains("~$"));
        } finally {
            expect.close();
            channel.disconnect();
            session.disconnect();
        }
    }

    private static void connectToSensortag(Expect expect) throws IOException {
        expect.sendLine("gatttool -b " + SENSORTAG_BLUETOOTH_ADDRESS + " --interactive");
        expect.expect(contains("[LE]>"));

        expect.sendLine("connect");
        expect.expect(contains("Connection successful"));
    }

    private static void enableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x26 0100"); // Enable temperature sensor notifications
        expect.sendLine("char-write-cmd 0x3c 0100"); // Enable humidity sensor notifications
    }

    private static void disableNotifications(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3c 0000"); // Disable humidity sensor notifications
        expect.sendLine("char-write-cmd 0x26 0000"); // Disable temperature sensor notifications
    }

    private static void readTemperature(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x29 01"); // Enable temperature sensor

        Result result = expect.expect(regexp("Notification handle = 0x0025 value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        System.out.println("Thermometer value: " + value);

        expect.sendLine("char-write-cmd 0x29 00"); // Disable temperature sensor
    }

    private static void readHumidity(Expect expect) throws IOException {
        expect.sendLine("char-write-cmd 0x3f 01"); // Enable humidity sensor

        Result result = expect.expect(regexp("Notification handle = 0x003b value: (?!00 00 00 00)(\\w{2} \\w{2} \\w{2} \\w{2})"));
        String value = result.group(1);
        System.out.println("Humidity value: " + value);

        expect.sendLine("char-write-cmd 0x3f 00"); // Disable humidity sensor
    }

}

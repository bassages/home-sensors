package nl.wiegman.sensortag;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.python.core.PyFloat;
import org.python.core.PyInstance;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.math.BigDecimal;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class SensorTagReader {

    private static final Logger LOG = LoggerFactory.getLogger(SensorTagReader.class);

    private static String GATTOOL_RESULTLINE_PREFIX = "Characteristic value/descriptor: ";
    private static String SENSORTAG_ID = "BC:6A:29:AC:7D:31";

    @Autowired
    private SensortagPersister sensortagPersister;

    private PythonInterpreter pythonInterpreter;

    @Value("${installation-directory}")
    private String installationDirectory;

    @Value("${sensortag.probetime.seconds}")
    private int sensortagProbeTimeInSeconds;

    @PostConstruct
    private void connectAndListenForData() throws Exception {
        LOG.info("Starting SensorTagReader");

        setupPythonInterpreter();

        try {
            String command = "sh " + installationDirectory+ "/ambienttemperature.sh " + sensortagProbeTimeInSeconds;

            LOG.info("Running command: " + command);

            Process process = Runtime.getRuntime().exec(command);

            final Thread inputStreamThread = new Thread() {
                @Override
                public void run() {
                    handleInputStream(process.getInputStream());
                }
            };
            inputStreamThread.start();

            final Thread errorStreamThread = new Thread() {
                @Override
                public void run() {
                    handleErrorStream(process.getErrorStream());
                }
            };
            errorStreamThread.start();

            process.waitFor();

        } catch (InterruptedException | IOException e) {
            LOG.error("Oops, and unexpected error occurred.", e);
        }
    }

    private void setupPythonInterpreter() throws Exception {
        LOG.debug("Setting up Python interpreter");

        File jythonLibDir = new File(installationDirectory, "jython-2.7.0");

        if (jythonLibDir.exists()) {
            LOG.debug("Unpacked jython jar directory already exists: " + jythonLibDir.getPath());
        } else {
            LOG.debug("Unpacked jython jar directory does not exist yet: " + jythonLibDir.getPath());
            extractJarWhichContainsClass(jythonLibDir, PythonInterpreter.class);
        }

        Properties props = new Properties();
        String lib = new File(jythonLibDir, "Lib").getPath();
        LOG.debug("Setting python.home to: " + lib);

        props.put("python.home", lib);
        props.put("python.import.site", "false");
        PythonInterpreter.initialize(System.getProperties(), props, new String[0]);
        pythonInterpreter = new PythonInterpreter();
    }

    private void extractJarWhichContainsClass(File destinationDirectory, Class<?> classInJar) throws IOException {
        URL urlOfJythonJar = classInJar.getProtectionDomain().getCodeSource().getLocation();
        JarURLConnection jarURLConnection = (JarURLConnection)urlOfJythonJar.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();

        Enumeration<JarEntry> entries = jarFile.entries();
        for(JarEntry je = entries.nextElement(); entries.hasMoreElements(); je = entries.nextElement()) {

            File entryFile = new File(destinationDirectory, je.getName());
            LOG.debug("Extracting " + entryFile.getPath());

            if (je.isDirectory()) {
                entryFile.mkdirs();
            } else {
                InputStream in = jarFile.getInputStream(je);

                try (FileOutputStream out = new FileOutputStream(entryFile)) {
                    byte[] buffer = new byte[4096];
                    int length = 0;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                        out.flush();
                    }
                }
            }
        }
    }

    private void handleInputStream(InputStream inputStream) {
        InputStreamReader in = new InputStreamReader(inputStream);
        try {
            LineIterator it = IOUtils.lineIterator(in);
            while (it.hasNext()) {
                processGatttoolOutputAmbientTemperature(it.nextLine());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void handleErrorStream(InputStream errorStream) {
        InputStreamReader in = new InputStreamReader(errorStream);
        try {
            LineIterator it = IOUtils.lineIterator(in);
            while (it.hasNext()) {
                LOG.error(it.nextLine());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void processGatttoolOutputAmbientTemperature(String gatttoolOutputLine) {
        try {
            if (gatttoolOutputLine.startsWith(GATTOOL_RESULTLINE_PREFIX) && !gatttoolOutputLine.endsWith("00 00 00 00")) {
                String hexValuesFromGattoolOutput = gatttoolOutputLine.replace(GATTOOL_RESULTLINE_PREFIX, "");
                BigDecimal bigDecimal = convertAmbientTemperature(hexValuesFromGattoolOutput);

                // Sometimes the meter reading is 0.0, which is strange and probably caused by a false reading. Ignore these values.
                if (bigDecimal != null && bigDecimal.doubleValue() != 0.0d) {
                    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_CEILING);
                    LOG.info("Temperature: " + bigDecimal.doubleValue());
                    sensortagPersister.persist(bigDecimal);
                } else {
                    LOG.warn("Ignoring invalid output from gattool: " + gatttoolOutputLine);
                }

            } else {
                LOG.warn("Invalid result from gattool: " + gatttoolOutputLine);
            }

        } catch (NumberFormatException e) {
            LOG.warn("Ignoring invalid temperature: " + gatttoolOutputLine);
        }
    }

    private BigDecimal convertAmbientTemperature(String hexString) {
        BigDecimal result = null;

        execPythonfile("ambientTemperatureConverter.py");

        PyInstance converter = createPythonClass("AmbientTemperatureConverter");
        PyObject convertResult = converter.invoke("fromHex", new PyString(hexString));
        if (convertResult instanceof PyFloat) {
            result = new BigDecimal(((PyFloat)convertResult).getValue());
        } else {
            LOG.warn("Unexpected result from AmbientTemperatureConverter: " + result);
        }
        return result;
    }

    private void execPythonfile(String fileName) {
        pythonInterpreter.execfile(this.getClass().getResourceAsStream(fileName));
    }

    private PyInstance createPythonClass(String className) {
        return (PyInstance) this.pythonInterpreter.eval(className + "()");
    }
}

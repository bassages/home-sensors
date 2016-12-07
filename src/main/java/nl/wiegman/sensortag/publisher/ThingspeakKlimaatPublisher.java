package nl.wiegman.sensortag.publisher;

import com.angryelectron.thingspeak.Channel;
import com.angryelectron.thingspeak.Entry;
import com.angryelectron.thingspeak.ThingSpeakException;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class ThingspeakKlimaatPublisher implements KlimaatPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ThingspeakKlimaatPublisher.class);

    @Value("${thingspeak-klimaat-channelid:#{null}}")
    private Integer channelId;
    @Value("${thingspeak-write-api-key:#{null}}")
    private String writeApiKey;

    private Long lastUpload = null;

    @Async
    @Override
    public void publish(BigDecimal temperatuur, BigDecimal luchtvochtigheid) {

        if (channelId != null && StringUtils.isNotBlank(writeApiKey) && lastUpdateWasTooLongAgo()) {

            try {
                Channel channel = new Channel(channelId, writeApiKey);

                Entry entry = new Entry();
                entry.setField(1, temperatuur.toString());
                entry.setField(2, luchtvochtigheid.toString());

                channel.update(entry);

                lastUpload = System.currentTimeMillis();

            } catch (UnirestException | ThingSpeakException e) {
                LOG.warn("Failed to publish klimaat to Thingspeak", e);
            }
        }
    }

    private boolean lastUpdateWasTooLongAgo() {
        return lastUpload == null || (System.currentTimeMillis() - lastUpload) > TimeUnit.MINUTES.toMillis(9);
    }
}

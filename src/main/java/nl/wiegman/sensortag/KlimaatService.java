package nl.wiegman.sensortag;

import nl.wiegman.sensortag.publisher.KlimaatPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class KlimaatService {
    private static final Logger LOG = LoggerFactory.getLogger(KlimaatService.class);

    @Autowired
    private List<KlimaatPublisher> klimaatPublishers;

    public void publish(BigDecimal temperatuur, BigDecimal luchtvochtigheid) {
        LOG.debug("Publishing to {} publishers", klimaatPublishers.size());
        klimaatPublishers.forEach(publisher -> publisher.publish(temperatuur, luchtvochtigheid));
    }
}

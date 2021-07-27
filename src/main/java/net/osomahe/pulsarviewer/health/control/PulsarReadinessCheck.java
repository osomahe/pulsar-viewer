package net.osomahe.pulsarviewer.health.control;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class PulsarReadinessCheck implements HealthCheck {

    private static final Logger log = Logger.getLogger(PulsarReadinessCheck.class);

    @ConfigProperty(name = "pulsar.service-url")
    String serviceUrl;

    @ConfigProperty(name = "pulsar.default.reader")
    String readerName;

    @ConfigProperty(name = "pulsar.health.topic")
    String healthTopic;

    @Inject
    PulsarClient pulsarClient;

    private Reader<byte[]> reader;


    @Override
    @Timeout(500)
    public HealthCheckResponse call() {
        if (reader == null) {
            try {
                reader = pulsarClient.newReader().readerName(readerName).topic(healthTopic).startMessageId(MessageId.earliest).create();
            } catch (PulsarClientException e) {
                log.warnf("Cannot connect to Apache Pulsar on url: %s", serviceUrl);
            }
        }
        boolean connected = reader != null && reader.isConnected();
        if (!connected) {
            reader = null;
        }
        return HealthCheckResponse.builder()
                .name("Apache Pulsar connection health check")
                .withData("pulsarUrl", serviceUrl)
                .withData("connected", connected)
                .status(connected)
                .build();
    }
}

package net.osomahe.pulsarviewer.read.control;

import io.quarkus.runtime.ShutdownEvent;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.Optional;

@ApplicationScoped
public class PulsarClientFactory {

    private static final Logger log = Logger.getLogger(PulsarClientFactory.class);

    @ConfigProperty(name = "pulsar.service-url")
    String serviceUrl;

    @ConfigProperty(name = "pulsar.tls-trust-cert")
    Optional<String> tlsTrustCert;

    @ConfigProperty(name = "pulsar.tls-cert-file")
    Optional<String> tlsCertFile;

    @ConfigProperty(name = "pulsar.tls-key-file")
    Optional<String> tlsKeyFile;

    PulsarClient pulsarClient;

    private void init() {
        try {
            ClientBuilder clientBuilder = PulsarClient.builder().serviceUrl(serviceUrl);
            if (tlsTrustCert.isPresent()) {
                clientBuilder = clientBuilder.tlsTrustCertsFilePath(tlsTrustCert.get());
            }
            if (tlsCertFile.isPresent() && tlsKeyFile.isPresent()) {
                clientBuilder = clientBuilder.authentication(
                        "org.apache.pulsar.client.impl.auth.AuthenticationTls",
                        "tlsCertFile:" + tlsCertFile.get() + ",tlsKeyFile:" + tlsKeyFile.get()
                );
            }

            this.pulsarClient = clientBuilder.build();
        } catch (PulsarClientException e) {
            log.error("Cannot create PulsarClient instance for service url: " + serviceUrl, e);
        }
    }

    @Produces
    public PulsarClient getPulsarClient() {
        if (this.pulsarClient == null) {
            init();
        }
        return this.pulsarClient;
    }

    void shutdown(@Observes ShutdownEvent event) {
        try {
            this.pulsarClient.close();
        } catch (PulsarClientException e) {
            log.error("Cannot close pulsar client connection", e);
        }
    }

}

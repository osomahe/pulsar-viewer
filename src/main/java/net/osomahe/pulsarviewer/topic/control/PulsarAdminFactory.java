package net.osomahe.pulsarviewer.topic.control;

import io.quarkus.runtime.ShutdownEvent;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.client.api.PulsarClientException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.Optional;

@ApplicationScoped
public class PulsarAdminFactory {

    private static final Logger log = Logger.getLogger(PulsarAdminFactory.class);

    @ConfigProperty(name = "pulsar.admin.service-http-url")
    Optional<String> serviceHttpUrl;

    @ConfigProperty(name = "pulsar.tls-trust-cert")
    Optional<String> tlsTrustCert;

    @ConfigProperty(name = "pulsar.admin.tls-cert-file")
    Optional<String> tlsCertAdminFile;

    @ConfigProperty(name = "pulsar.admin.tls-key-file")
    Optional<String> tlsKeyAdminFile;

    PulsarAdmin pulsarAdmin;

    private void init() {
        try {
            if(serviceHttpUrl.isEmpty()){
                log.info("Pulsar admin connection failed. No service http url available.");
                return;
            }
            PulsarAdminBuilder adminBuilder = PulsarAdmin.builder().serviceHttpUrl(serviceHttpUrl.get());
            if (tlsTrustCert.isPresent()) {
                adminBuilder = adminBuilder.tlsTrustCertsFilePath(tlsTrustCert.get());
            }else {
                adminBuilder.allowTlsInsecureConnection(true);
            }
            if (tlsCertAdminFile.isPresent() && tlsKeyAdminFile.isPresent()) {
                adminBuilder = adminBuilder.authentication(
                        "org.apache.pulsar.client.impl.auth.AuthenticationTls",
                        "tlsCertFile:" + tlsCertAdminFile.get() + ",tlsKeyFile:" + tlsKeyAdminFile.get()
                );
            }

            this.pulsarAdmin = adminBuilder.build();
        } catch (PulsarClientException e) {
            log.error("Cannot create PulsarClient instance for service url: " + serviceHttpUrl.orElse(""), e);
        }
    }

    @Produces
    public PulsarAdmin getPulsarAdmin() {
        if (this.pulsarAdmin == null) {
            init();
        }
        return this.pulsarAdmin;
    }

    void shutdown(@Observes ShutdownEvent event) {
        this.pulsarAdmin.close();
    }
}

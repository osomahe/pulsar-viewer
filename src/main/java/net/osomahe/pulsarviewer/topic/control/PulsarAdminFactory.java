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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogManager;

@ApplicationScoped
public class PulsarAdminFactory {

    private static final Logger log = Logger.getLogger(PulsarAdminFactory.class);

    @ConfigProperty(name = "pulsar.admin.service-http-url")
    Optional<String> serviceHttpUrl;

    @ConfigProperty(name = "pulsar.admin.tls-cert-file")
    Optional<String> tlsCertAdminFile;

    @ConfigProperty(name = "pulsar.admin.tls-key-file")
    Optional<String> tlsKeyAdminFile;

    PulsarAdmin pulsarAdmin;

    private void init() {
        try {
            if (serviceHttpUrl.isEmpty()) {
                log.info("Pulsar admin connection failed. No service http url available.");
                return;
            }

            var savedHandlers = saveLogHandlers();

            PulsarAdminBuilder adminBuilder = PulsarAdmin.builder().serviceHttpUrl(serviceHttpUrl.get()).allowTlsInsecureConnection(true);

            if (tlsCertAdminFile.isPresent() && tlsKeyAdminFile.isPresent()) {
                adminBuilder = adminBuilder.authentication(
                        "org.apache.pulsar.client.impl.auth.AuthenticationTls",
                        "tlsCertFile:" + tlsCertAdminFile.get() + ",tlsKeyFile:" + tlsKeyAdminFile.get()
                );
            }

            this.pulsarAdmin = adminBuilder.build();

            restoreLogHandlers(savedHandlers);

        } catch (PulsarClientException e) {
            log.error("Cannot create PulsarClient instance for service url: " + serviceHttpUrl.orElse(""), e);
        }
    }

    /**
     * Handlers need to be saved because {@link org.apache.pulsar.client.admin.internal.PulsarAdminImpl}
     * removes them and creates bridge and that breaks all logging in quarkus.
     * Log handlers are restored by method restoreLogHandlers() after PulsarAdmin init is completed.
     *
     * @return
     */
    private List<Handler> saveLogHandlers() {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        var savedHandlers = new ArrayList<java.util.logging.Handler>();
        for (var handler : rootLogger.getHandlers()) {
            savedHandlers.add(handler);
        }
        return savedHandlers;
    }

    /**
     * Check docs in method saveLogHandlers()
     *
     * @param savedHandlers
     */
    private void restoreLogHandlers(List<Handler> savedHandlers) {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        for (var handler : savedHandlers) {
            rootLogger.addHandler(handler);
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

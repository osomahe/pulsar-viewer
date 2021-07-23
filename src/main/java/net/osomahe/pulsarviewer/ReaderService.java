package net.osomahe.pulsarviewer;

import org.apache.pulsar.client.api.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ReaderService {

    private static final Logger log = Logger.getLogger(ReaderService.class);

    @ConfigProperty(name = "pulsar.default.reader")
    String readerName;

    @Inject
    PulsarClient pulsarClient;

    public List<ReaderMessage> readStringMessage(String topicName) {

        try (Reader<String> reader = pulsarClient.newReader(Schema.STRING)
                .readerName(readerName)
                .topic(topicName)
                .startMessageId(MessageId.earliest)
                .create()) {
            List<ReaderMessage> messages = new ArrayList<>();
            while (true) {
                var message = reader.readNext(1, TimeUnit.SECONDS);
                if (message == null) {
                    break;
                }
                messages.add(new ReaderMessage(message));
            }
            return messages;
        } catch (PulsarClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}

package net.osomahe.pulsarviewer;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ReaderService {

    private static final Logger log = Logger.getLogger(ReaderService.class);

    @ConfigProperty(name = "pulsar.default.reader")
    String readerName;

    @Inject
    PulsarClient pulsarClient;

    public List<ReaderMessage> readStringMessage(String topicName, Optional<String> jsonPathPredicate) {

        try (Reader<String> reader = pulsarClient.newReader(Schema.STRING)
                .readerName(readerName)
                .topic(topicName)
                .startMessageId(MessageId.earliest)
                .create()) {
            List<ReaderMessage> messages = new ArrayList<>();
            while (reader.hasMessageAvailable()) {
                var message = reader.readNext(1, TimeUnit.SECONDS);
                if (message == null) {
                    break;
                }
                var readerMessage = new ReaderMessage(message);
                if (jsonPathPredicate.isPresent()) {
                    try {
                        List result = JsonPath.parse(readerMessage.value).read(jsonPathPredicate.get());
                        if (result.size() > 0) {
                            messages.add(readerMessage);
                        }
                    } catch (InvalidPathException e) {
                        log.debugf("Invalid JsonPath: %s for data: %s", readerMessage.value);
                    }
                } else {
                    messages.add(readerMessage);
                }
            }
            return messages;
        } catch (IOException e) {
            log.error("Cannot create pulsar reader for topic: " + topicName);
        }
        return Collections.emptyList();
    }

    public Optional<ReaderMessage> readStringMessage(String topicName, String messageId) {
        try (Reader<String> reader = pulsarClient.newReader(Schema.STRING)
                .readerName(readerName)
                .startMessageIdInclusive()
                .startMessageId(getMessageId(messageId))
                .topic(topicName)
                .create()) {
            Message<String> message = reader.readNext();
            if (message != null) {
                return Optional.of(new ReaderMessage(message));
            }
        } catch (IOException e) {
            log.error("Cannot create pulsar reader for topic: " + topicName);
        }
        return Optional.empty();
    }

    private MessageId getMessageId(String messageId) {
        String[] parts = messageId.split(":");
        return DefaultImplementation.newMessageId(
                Long.parseLong(parts[0]), Long.parseLong(parts[1]), Integer.parseInt(parts[2]));
    }
}

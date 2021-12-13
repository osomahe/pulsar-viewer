package net.osomahe.pulsarviewer.read.control;

import com.jayway.jsonpath.JsonPath;
import net.osomahe.pulsarviewer.read.entity.PulsarReaderException;
import net.osomahe.pulsarviewer.read.entity.ReaderMessage;
import net.osomahe.pulsarviewer.topic.boundary.TopicFacade;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReaderService {

    private static final Logger log = Logger.getLogger(ReaderService.class);

    @ConfigProperty(name = "pulsar.default.reader")
    String readerName;

    @Inject
    PulsarClient pulsarClient;

    @Inject
    TopicFacade facadeTopic;

    public List<ReaderMessage> readStringMessage(String topicName, Optional<String> messageId, Optional<String> key, Optional<String> jsonPathPredicate, Optional<Long> fromEpochSecs, Optional<Long> toEpochSecs) {
        log.infof("Reading messages from topic[%s], messageId[%s], jsonPredicate[%s], fromEpochSecs[%s], toEpochSecs[%s]", topicName, messageId, jsonPathPredicate, fromEpochSecs, toEpochSecs);
        List<ReaderMessage> messages = new ArrayList<>();
        List<String> topics = facadeTopic.getTopics(topicName);
        log.info("Reading messages from " + topics.size() + " topics");
        var futures = topics.stream()
                .map(topic -> CompletableFuture.supplyAsync(() -> readSingleTopicMessages(topic, messageId, key, jsonPathPredicate, fromEpochSecs, toEpochSecs)))
                .collect(Collectors.toList());

        for (var future : futures) {
            messages.addAll(future.join());
        }

        log.info("Reading messages complete");
        Collections.sort(messages);
        Collections.reverse(messages);
        return messages;
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, Optional<String> messageId, Optional<String> key, Optional<String> jsonPathPredicate, Optional<Long> fromEpochSecs, Optional<Long> toEpochSecs) {
        if (messageId.isPresent()) {
            return readSingleMessage(topicName, messageId.get());
        }
        return readSingleTopicMessages(topicName, key, jsonPathPredicate, fromEpochSecs, toEpochSecs);
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, Optional<String> key, Optional<String> jsonPathPredicate, Optional<Long> fromEpochSecs, Optional<Long> toEpochSecs) {
        return readSingleTopicMessagesByTime(topicName, fromEpochSecs, toEpochSecs.orElse(Instant.now().getEpochSecond())).stream()
                .filter(msg -> key.isEmpty() || key.get().equals(msg.key))
                .filter(msg -> jsonPathPredicate.isEmpty() || matchJsonPath(msg, jsonPathPredicate.get()))
                .collect(Collectors.toList());
    }

    private boolean matchJsonPath(ReaderMessage msg, String jsonPathPredicate) {
        try {
            List result = JsonPath.parse(msg.payload).read(jsonPathPredicate);
            return result != null && result.size() > 0;
        } catch (Exception e) {
            log.debugf("Invalid JsonPath: %s for data: %s", jsonPathPredicate, msg.payload);
        }
        return false;
    }

    private List<ReaderMessage> readSingleTopicMessagesByTime(String topicName, Optional<Long> fromEpochSecs, Long toEpochSecs) {
        try (Reader<byte[]> reader = pulsarClient.newReader()
                .readerName(readerName)
                .topic(topicName)
                .startMessageId(MessageId.earliest)
                .create()) {
            List<ReaderMessage> messages = new ArrayList<>();
            if (fromEpochSecs.isPresent()) {
                reader.seek(fromEpochSecs.get() * 1_000);
            }
            long toEpochMillis = toEpochSecs * 1_000;
            while (reader.hasMessageAvailable()) {
                var message = reader.readNext(1, TimeUnit.SECONDS);
                if (message == null) {
                    break;
                }
                if (message.getPublishTime() > toEpochMillis) {
                    break;
                }
                messages.add(new ReaderMessage(message));
            }
            return messages;
        } catch (IOException e) {
            throw new PulsarReaderException(topicName, e);
        }
    }

    private List<ReaderMessage> readSingleMessage(String topicName, String messageId) {
        Optional<MessageId> oMessageId = getMessageId(messageId);
        if (oMessageId.isPresent()) {
            try (Reader<byte[]> reader = pulsarClient.newReader()
                    .readerName(readerName)
                    .startMessageIdInclusive()
                    .startMessageId(oMessageId.get())
                    .topic(topicName)
                    .create()) {
                Message<byte[]> message = reader.readNext();
                if (message != null) {
                    ReaderMessage readerMessage = new ReaderMessage(message);
                    if (messageId.equals(readerMessage.messageId)) {
                        return Collections.singletonList(readerMessage);
                    }
                }
            } catch (IOException e) {
                throw new PulsarReaderException(topicName, messageId, e);
            }
        }
        return Collections.emptyList();
    }

    private Optional<MessageId> getMessageId(String messageId) {
        try {
            String[] parts = messageId.split(":");
            return Optional.ofNullable(DefaultImplementation.newMessageId(
                    Long.parseLong(parts[0]), Long.parseLong(parts[1]), Integer.parseInt(parts[2])));
        } catch (Exception e) {
            log.error("Cannot parse message ID: " + messageId, e);
        }
        return Optional.empty();
    }
}

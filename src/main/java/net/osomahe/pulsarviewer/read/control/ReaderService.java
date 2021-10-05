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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    public List<ReaderMessage> readStringMessage(String topicName, Optional<String> messageId, Optional<String> jsonPathPredicate, Optional<Integer> lastMins) {
        List<ReaderMessage> messages = new ArrayList<>();
        List<String> topics;
        if (topicName.endsWith("*")) {
            topics = facadeTopic.getTopics(topicName);
        } else {
            topics = Collections.singletonList(topicName);
        }
        for (String topic : topics) {
            messages.addAll(readSingleTopicMessages(topic, messageId, jsonPathPredicate));
        }

        if (lastMins.isPresent()) {
            long lastTime = System.currentTimeMillis() - (lastMins.get() * 60 * 1_000);
            messages = messages.stream().filter(msg -> msg.publishTime > lastTime).collect(Collectors.toList());
        }

        Collections.sort(messages);
        Collections.reverse(messages);
        return messages;
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, Optional<String> messageId, Optional<String> jsonPathPredicate) {
        if (messageId.isPresent()) {
            return readSingleTopicMessages(topicName, messageId.get());
        }
        return readSingleTopicMessages(topicName, jsonPathPredicate);
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, Optional<String> jsonPathPredicate) {
        try (Reader<byte[]> reader = pulsarClient.newReader()
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
                        List result = JsonPath.parse(readerMessage.payload).read(jsonPathPredicate.get());
                        if (result.size() > 0) {
                            messages.add(readerMessage);
                        }
                    } catch (Exception e) {
                        log.debugf("Invalid JsonPath: %s for data: %s", readerMessage.payload);
                    }
                } else {
                    messages.add(readerMessage);
                }
            }
            return messages;
        } catch (IOException e) {
            throw new PulsarReaderException(topicName, e);
        }
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, String messageId) {
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

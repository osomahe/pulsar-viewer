package net.osomahe.pulsarviewer.read.control;

import com.jayway.jsonpath.JsonPath;
import net.osomahe.pulsarviewer.read.entity.PulsarReaderException;
import net.osomahe.pulsarviewer.read.entity.ReaderFilter;
import net.osomahe.pulsarviewer.read.entity.ReaderInfo;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@ApplicationScoped
public class ReaderService {

    private static final Logger log = Logger.getLogger(ReaderService.class);

    @ConfigProperty(name = "pulsar.default.reader")
    String readerName;

    @ConfigProperty(name = "pulsar.max-messages")
    Long maxMessages;

    @ConfigProperty(name = "pulsar.default-max-message-age-seconds")
    Long defaultMaxMessageAgeSeconds;

    @Inject
    PulsarClient pulsarClient;

    @Inject
    TopicFacade facadeTopic;

    public ReaderInfo readStringMessage(ReaderFilter readerFilter) {
        log.infof("Reading messages by filter: %s", readerFilter);
        List<ReaderMessage> messages = new ArrayList<>();
        List<String> topics = facadeTopic.getTopics(readerFilter.getTopicName());
        log.info("Reading messages from " + topics.size() + " topics");
        AtomicLong msgCount = new AtomicLong(0);
        var futures = topics.stream()
                .map(topic -> CompletableFuture.supplyAsync(() -> readSingleTopicMessages(topic, readerFilter, msgCount)))
                .collect(Collectors.toList());

        for (var future : futures) {
            messages.addAll(future.join());
        }

        log.info("Reading messages complete");
        Collections.sort(messages);
        Collections.reverse(messages);

        if (msgCount.longValue() >= maxMessages.longValue()) {
            return new ReaderInfo(
                    messages,
                    String.format("Error! Maximum number of messages loaded [%s] from Pulsar was exceeded. Limit the results by time please.", maxMessages)
            );
        }
        return new ReaderInfo(messages);
    }

    private List<ReaderMessage> readSingleTopicMessages(String topicName, ReaderFilter readerFilter, AtomicLong msgCount) {
        if (readerFilter.getMessageId() != null) {
            return readSingleMessage(topicName, readerFilter.getMessageId());
        }
        return readSingleTopicFilteredMessages(topicName, readerFilter, msgCount);
    }

    private List<ReaderMessage> readSingleTopicFilteredMessages(String topicName, ReaderFilter filter, AtomicLong msgCount) {
        return readSingleTopicMessagesByTime(topicName, filter, msgCount).stream()
                .filter(msg -> filter.getKey() == null || filter.getKey().equals(msg.key))
                .filter(msg -> filter.getJsonPathPredicate() == null || matchJsonPath(msg, filter.getJsonPathPredicate()))
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

    private List<ReaderMessage> readSingleTopicMessagesByTime(String topicName, ReaderFilter filter, AtomicLong msgCount) {
        Long toEpochSecs = filter.getToEpochSecs() != null ? filter.getToEpochSecs() : Instant.now().getEpochSecond();
        Long fromEpochSecs = filter.getFromEpochSecs() != null ? filter.getFromEpochSecs() : toEpochSecs - defaultMaxMessageAgeSeconds;

        try (Reader<byte[]> reader = pulsarClient.newReader()
                .readerName(readerName)
                .topic(topicName)
                .startMessageId(MessageId.earliest)
                .create()) {
            List<ReaderMessage> messages = new ArrayList<>();

            reader.seek(fromEpochSecs * 1_000);

            long toEpochMillis = toEpochSecs * 1_000;
            while (reader.hasMessageAvailable()) {
                if (msgCount.incrementAndGet() > maxMessages) {
                    log.infof("Stopping reading topic %s. Too much messages already read from Pulsar by filter %s.", topicName, filter);
                    break;
                }
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
            return Optional.ofNullable(DefaultImplementation.getDefaultImplementation().newMessageId(
                    Long.parseLong(parts[0]), Long.parseLong(parts[1]), Integer.parseInt(parts[2])));
        } catch (Exception e) {
            log.error("Cannot parse message ID: " + messageId, e);
        }
        return Optional.empty();
    }
}

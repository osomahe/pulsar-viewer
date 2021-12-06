package net.osomahe.pulsarviewer.read.entity;

import org.apache.pulsar.client.api.Message;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class ReaderMessage implements Comparable<ReaderMessage> {
    public final String messageId;
    public final String topic;
    public final long publishTime;
    public final String producer;
    public final String key;
    public final String payload;

    public ReaderMessage(Message<byte[]> message) {
        messageId = message.getMessageId().toString();
        publishTime = message.getPublishTime();
        producer = message.getProducerName();
        topic = message.getTopicName();
        key = message.getKey();
        payload = new String(message.getValue(), StandardCharsets.UTF_8);
    }


    @Override
    public int compareTo(ReaderMessage rm) {
        if (rm == null) {
            return 1;
        }
        int result = Long.compare(publishTime, rm.publishTime);
        if (result == 0) {
            result = topic.compareTo(rm.topic);
        }
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReaderMessage.class.getSimpleName() + "[", "]")
                .add("messageId='" + messageId + "'")
                .add("topic='" + topic + "'")
                .add("publishTime=" + publishTime)
                .add("producer='" + producer + "'")
                .add("payload='" + payload + "'")
                .toString();
    }
}

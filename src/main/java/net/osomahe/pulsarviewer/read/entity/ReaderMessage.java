package net.osomahe.pulsarviewer.read.entity;

import org.apache.pulsar.client.api.Message;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class ReaderMessage {
    public final String messageId;
    public final long publishTime;
    public final String producer;
    public final String payload;

    public ReaderMessage(Message<byte[]> message) {
        messageId = message.getMessageId().toString();
        publishTime = message.getPublishTime();
        producer = message.getProducerName();
        payload = new String(message.getValue(), StandardCharsets.UTF_8);
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", ReaderMessage.class.getSimpleName() + "[", "]")
                .add("messageId='" + messageId + "'")
                .add("publishTime=" + publishTime)
                .add("producer='" + producer + "'")
                .add("payload='" + payload + "'")
                .toString();
    }
}

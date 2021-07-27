package net.osomahe.pulsarviewer.read.entity;

import java.io.IOException;

public class PulsarReaderException extends RuntimeException {

    public PulsarReaderException(String topicName, IOException e) {
        super("Cannot create pulsar reader for topic: " + topicName, e);
    }

    public PulsarReaderException(String topicName, String messageId, IOException e) {
        super("Cannot create pulsar reader for topic: " + topicName + ", and messageId: " + messageId, e);
    }
}

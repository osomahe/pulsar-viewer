package net.osomahe.pulsarviewer.read.entity;

import java.util.List;


public class ReaderInfo {

    public final List<ReaderMessage> messages;

    public final String errorMessage;

    public ReaderInfo(List<ReaderMessage> messages, String errorMessage) {
        this.messages = messages;
        this.errorMessage = errorMessage;
    }

    public ReaderInfo(List<ReaderMessage> messages) {
        this(messages, null);
    }
}

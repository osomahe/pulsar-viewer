package net.osomahe.pulsarviewer.read.entity;

import java.util.StringJoiner;


public class ReaderFilter {

    private String topicName;

    private String messageId;

    private String key;

    private String jsonPathPredicate;

    private Long fromEpochSecs;

    private Long toEpochSecs;

    private ReaderFilter() {

    }

    public String getTopicName() {
        return topicName;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getKey() {
        return key;
    }

    public String getJsonPathPredicate() {
        return jsonPathPredicate;
    }

    public Long getFromEpochSecs() {
        return fromEpochSecs;
    }

    public Long getToEpochSecs() {
        return toEpochSecs;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ReaderFilter.class.getSimpleName() + "[", "]")
                .add("topicName='" + topicName + "'")
                .add("messageId='" + messageId + "'")
                .add("key='" + key + "'")
                .add("jsonPathPredicate='" + jsonPathPredicate + "'")
                .add("fromEpochSecs=" + fromEpochSecs)
                .add("toEpochSecs=" + toEpochSecs)
                .toString();
    }


    public static Builder builder() {return new Builder();}

    public static final class Builder {
        private String topicName;

        private String messageId;

        private String key;

        private String jsonPathPredicate;

        private Long fromEpochSecs;

        private Long toEpochSecs;

        private Builder() {}


        public Builder withTopicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder withMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withJsonPathPredicate(String jsonPathPredicate) {
            this.jsonPathPredicate = jsonPathPredicate;
            return this;
        }

        public Builder withFromEpochSecs(Long fromEpochSecs) {
            this.fromEpochSecs = fromEpochSecs;
            return this;
        }

        public Builder withToEpochSecs(Long toEpochSecs) {
            this.toEpochSecs = toEpochSecs;
            return this;
        }

        public ReaderFilter build() {
            ReaderFilter readerFilter = new ReaderFilter();
            readerFilter.messageId = this.messageId;
            readerFilter.toEpochSecs = this.toEpochSecs;
            readerFilter.fromEpochSecs = this.fromEpochSecs;
            readerFilter.topicName = this.topicName;
            readerFilter.key = this.key;
            readerFilter.jsonPathPredicate = this.jsonPathPredicate;
            return readerFilter;
        }
    }
}

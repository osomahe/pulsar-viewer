package net.osomahe.pulsarviewer.topic.boundary;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopicFacade {
    private static final Logger log = Logger.getLogger(TopicFacade.class);

    @Inject
    PulsarAdmin pulsarAdmin;

    public List<String> getTopics(String pattern) {
        if (pattern == null || !pattern.endsWith("*")) {
            return Collections.singletonList(pattern);
        }
        String namespace = getNamespace(pattern);
        String topicPrefix = pattern.substring(0, pattern.length() - 1).toLowerCase();
        try {
            return pulsarAdmin.namespaces().getTopics(namespace)
                    .stream()
                    .filter(topic -> topic.toLowerCase().startsWith(topicPrefix))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Finding topics for pattern failed. pattern = " + pattern, e);
        }
        return Collections.emptyList();
    }

    private String getNamespace(String pattern) {
        int lastSlash = pattern.lastIndexOf("/");
        int lastDouble = pattern.lastIndexOf("//");
        return pattern.substring(lastDouble + 2, lastSlash);
    }
}

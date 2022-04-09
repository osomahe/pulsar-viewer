package net.osomahe.pulsarviewer.read.boundary;

import net.osomahe.pulsarviewer.read.control.ReaderService;
import net.osomahe.pulsarviewer.read.entity.ReaderFilter;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


@Path("/read")
public class ReaderResource {

    @Inject
    ReaderService service;

    @GET
    public Response readMessages(
            @NotEmpty @QueryParam("topic") String topicName,
            @QueryParam("messageId") String messageId,
            @QueryParam("jsonPathPredicate") String jsonPathPredicate,
            @QueryParam("key") String key,
            @QueryParam("from") Long fromEpochSecs,
            @QueryParam("to") Long toEpochSecs
    ) {

        ReaderFilter filter = ReaderFilter.builder()
                .withTopicName(topicName)
                .withMessageId(messageId)
                .withJsonPathPredicate(jsonPathPredicate)
                .withKey(key)
                .withFromEpochSecs(fromEpochSecs)
                .withToEpochSecs(toEpochSecs)
                .build();
        return Response.ok(service.readStringMessage(filter)).build();
    }
}

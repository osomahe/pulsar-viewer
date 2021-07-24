package net.osomahe.pulsarviewer;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/read")
public class ReaderResource {

    @Inject
    ReaderService service;

    @GET
    public Response readMessages(
            @NotEmpty @QueryParam("topic") String topicName,
            @QueryParam("messageId") Optional<String> messageId,
            @QueryParam("jsonPathPredicate") Optional<String> jsonPathPredicate
    ) {
        if (messageId.isPresent()) {
            Optional<ReaderMessage> message = service.readStringMessage(topicName, messageId.get());
            if (message.isPresent()) {
                return Response.ok(message).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(service.readStringMessage(topicName, jsonPathPredicate)).build();
    }
}

package net.osomahe.pulsarviewer.read.boundary;

import net.osomahe.pulsarviewer.read.control.ReaderService;

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
            @QueryParam("jsonPathPredicate") Optional<String> jsonPathPredicate,
            @QueryParam("lastMins") Optional<Integer> lastMins
    ) {
        return Response.ok(service.readStringMessage(topicName, messageId, jsonPathPredicate, lastMins)).build();
    }
}

package net.osomahe.pulsarviewer;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/read")
public class ReaderResource {

    @Inject
    ReaderService service;

    @GET
    public Response readMessages(@QueryParam("topic") String topicName) {
        return Response.ok(service.readStringMessage(topicName)).build();
    }
}

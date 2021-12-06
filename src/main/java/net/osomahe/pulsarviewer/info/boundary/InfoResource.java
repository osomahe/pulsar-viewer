package net.osomahe.pulsarviewer.info.boundary;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/info")
public class InfoResource {

    @ConfigProperty(name = "quarkus.application.version")
    String version;

    @ConfigProperty(name = "quarkus.application.name")
    String name;

    @GET
    @PermitAll
    public Response getInfo() {
        return Response.ok().entity(Map.of("name", name, "version", version)).build();

    }
}

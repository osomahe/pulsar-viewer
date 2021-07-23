package net.osomahe.pulsarviewer;

import org.jboss.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;
import java.util.UUID;

@Provider
@Priority(1)
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger log = Logger.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        String errorId = UUID.randomUUID().toString();
        log.error("errorId: " + errorId, exception);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("errorId", errorId))
                .build();
    }


}

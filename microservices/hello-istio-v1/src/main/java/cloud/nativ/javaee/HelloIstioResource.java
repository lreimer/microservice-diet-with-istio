package cloud.nativ.javaee;

import lombok.extern.java.Log;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Log
@ApplicationScoped
@Path("hello")
public class HelloIstioResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public Response helloV1() {
        LOGGER.info("Hello Istio (v1).");
        JsonObject response = Json.createObjectBuilder()
                .add("message", "Hello Istio.")
                .add("version", "1.0.1")
                .build();
        return Response.ok(response).build();
    }

}

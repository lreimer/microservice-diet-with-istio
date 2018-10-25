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
import java.util.concurrent.TimeUnit;

@Log
@ApplicationScoped
@Path("hello")
public class HelloIstioResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public Response helloIstio() {
        LOGGER.info("Hello Istion (v2).");
        sleep();
        JsonObject response = Json.createObjectBuilder()
                .add("message", "Hello Istio.")
                .add("version", "2.0.1")
                .build();
        return Response.ok(response).build();
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

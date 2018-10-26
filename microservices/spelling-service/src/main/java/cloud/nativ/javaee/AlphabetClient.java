package cloud.nativ.javaee;

import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.*;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
@ApplicationScoped
public class AlphabetClient {

    @Inject
    @ConfigProperty(name = "a.service.url", defaultValue = "http://a-service:8080/api/alphabet/{a}")
    private String aServiceUrl;

    @Inject
    @ConfigProperty(name = "b.service.url", defaultValue = "http://b-service:8080/api/alphabet/{b}")
    private String bServiceUrl;

    @Inject
    @ConfigProperty(name = "c.service.url", defaultValue = "http://c-service:8080/api/alphabet/{c}")
    private String cServiceUrl;

    @Inject
    @ConfigProperty(name = "alphabet.service.url", defaultValue = "http://alphabet-service:8080/api/alphabet/{character}")
    private String alphabetServiceUrl;

    private Client client;

    @PostConstruct
    void initialize() {
        client = ClientBuilder.newBuilder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    @PreDestroy
    void destroy() {
        client.close();
    }

    @CircuitBreaker(delay = 5, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Timeout(value = 1, unit = ChronoUnit.SECONDS)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public String getA(Locale locale) {
        return client.target(aServiceUrl).resolveTemplate("a", "a")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 5, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public String getB(Locale locale) {
        return client.target(bServiceUrl).resolveTemplate("b", "b")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 5, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public String getC(Locale locale) {
        return client.target(cServiceUrl).resolveTemplate("c", "c")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 5, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS, absolute = true)
    public String getAny(char character, Locale locale) {
        return client.target(alphabetServiceUrl).resolveTemplate("character", Character.toString(character))
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @ApplicationScoped
    public static class StringFallbackHandler implements FallbackHandler<String> {
        @Override
        public String handle(ExecutionContext context) {
            LOGGER.log(Level.WARNING, "Handling fallback for {0}.", context.getMethod().getName());
            return "?";
        }
    }
}

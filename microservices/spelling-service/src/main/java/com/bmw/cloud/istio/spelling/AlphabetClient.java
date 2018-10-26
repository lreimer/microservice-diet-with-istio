package com.bmw.cloud.istio.spelling;

import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@RequestScoped
@Log
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

    @Inject
    private TracingRequestFilter tracingFilter;

    private Client client;

    @PostConstruct
    void initialize() {
        client = ClientBuilder.newBuilder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    @PreDestroy
    void destroy() {
        client.close();
    }

    @CircuitBreaker(delay = 1, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS)
    public String getA(Locale locale) {
        return client.register(tracingFilter).target(aServiceUrl).resolveTemplate("a", "a")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 2, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS)
    public String getB(Locale locale) {
        return client.register(tracingFilter).target(bServiceUrl).resolveTemplate("b", "b")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 3, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS)
    public String getC(Locale locale) {
        return client.register(tracingFilter).target(cServiceUrl).resolveTemplate("c", "c")
                .request().acceptLanguage(locale)
                .get(String.class);
    }

    @CircuitBreaker(delay = 5, delayUnit = ChronoUnit.SECONDS, requestVolumeThreshold = 10)
    @Fallback(StringFallbackHandler.class)
    @Timed(unit = MetricUnits.MILLISECONDS)
    public String getAny(char character, Locale locale) {
        return client.register(tracingFilter).target(alphabetServiceUrl).resolveTemplate("character", Character.toString(character))
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

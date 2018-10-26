package com.bmw.cloud.istio.alphabet;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Health
public class AlwaysHealthyCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse
                .named("everythingOk")
                .up()
                .withData("message", "Everything is healthy. Thanks for asking!")
                .build();
    }
}

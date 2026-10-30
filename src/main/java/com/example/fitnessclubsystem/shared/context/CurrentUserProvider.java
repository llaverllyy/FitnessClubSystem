package com.example.fitnessclubsystem.shared.context;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    private final UUID userId;
    private final UUID clientId;

    public CurrentUserProvider(
            @Value("${app.demo.current-user-id}") UUID userId,
            @Value("${app.demo.current-client-id}") UUID clientId) {
        this.userId = userId;
        this.clientId = clientId;
    }

    public UUID userId() {
        return userId;
    }

    public UUID clientId() {
        return clientId;
    }
}
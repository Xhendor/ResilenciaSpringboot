package com.example.vehiculosapi.health;

import lombok.Getter;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppHealthIndicator {
    private final ApplicationEventPublisher eventPublisher;
    @Getter
    private boolean isReady = false;
    @Getter
    private boolean isLive = true;

    public AppHealthIndicator(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
        AvailabilityChangeEvent.publish(eventPublisher, this, 
            ready ? ReadinessState.ACCEPTING_TRAFFIC : ReadinessState.REFUSING_TRAFFIC);
    }

    public void setLive(boolean live) {
        this.isLive = live;
        AvailabilityChangeEvent.publish(eventPublisher, this,
                live ? LivenessState.CORRECT : LivenessState.BROKEN);
    }


}

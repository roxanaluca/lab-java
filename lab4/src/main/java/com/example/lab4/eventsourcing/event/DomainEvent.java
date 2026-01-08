package com.example.lab4.eventsourcing.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the Event Sourcing pattern.
 * Events are immutable records of state changes that have occurred.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final long version;
    private final Instant timestamp;
    private final String eventType;

    protected DomainEvent(String aggregateId, String aggregateType, long version) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.timestamp = Instant.now();
        this.eventType = this.getClass().getSimpleName();
    }

    public String getEventId() {
        return eventId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public long getVersion() {
        return version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId='%s', version=%d, timestamp=%s}",
                eventType, eventId, aggregateId, version, timestamp);
    }
}

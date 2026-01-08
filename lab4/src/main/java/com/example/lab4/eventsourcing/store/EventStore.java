package com.example.lab4.eventsourcing.store;

import com.example.lab4.eventsourcing.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Event Store implementation for persisting domain events.
 * In production, this would be backed by a database (PostgreSQL, EventStoreDB, etc.)
 */
@Component
public class EventStore {

    private static final Logger logger = LoggerFactory.getLogger(EventStore.class);

    // In-memory event storage (keyed by aggregateId)
    private final Map<String, List<StoredEvent>> eventsByAggregate = new ConcurrentHashMap<>();

    // Global event stream for projections
    private final List<StoredEvent> globalEventStream = Collections.synchronizedList(new ArrayList<>());

    // Event listeners for real-time projections
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * Append events to the store for an aggregate.
     * Implements optimistic concurrency with version checking.
     */
    public synchronized void appendEvents(String aggregateId, List<DomainEvent> events, long expectedVersion) {
        List<StoredEvent> existingEvents = eventsByAggregate.getOrDefault(aggregateId, new ArrayList<>());

        // Optimistic concurrency check
        long currentVersion = existingEvents.isEmpty() ? 0 : existingEvents.getLast().version();
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException(
                    "Concurrency conflict for aggregate " + aggregateId +
                            ". Expected version " + expectedVersion + ", but was " + currentVersion);
        }

        // Store events
        List<StoredEvent> newStoredEvents = new ArrayList<>();
        for (DomainEvent event : events) {
            StoredEvent storedEvent = new StoredEvent(
                    globalEventStream.size() + newStoredEvents.size() + 1, // Global sequence
                    event.getEventId(),
                    aggregateId,
                    event.getAggregateType(),
                    event.getEventType(),
                    event.getVersion(),
                    event.getTimestamp(),
                    event
            );
            newStoredEvents.add(storedEvent);
        }

        eventsByAggregate.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(newStoredEvents);
        globalEventStream.addAll(newStoredEvents);

        logger.info("Appended {} events for aggregate {} (version {} -> {})",
                events.size(), aggregateId, expectedVersion, expectedVersion + events.size());

        // Notify listeners
        for (StoredEvent storedEvent : newStoredEvents) {
            notifyListeners(storedEvent);
        }
    }

    /**
     * Get all events for an aggregate.
     */
    public List<DomainEvent> getEvents(String aggregateId) {
        return eventsByAggregate.getOrDefault(aggregateId, List.of())
                .stream()
                .map(StoredEvent::event)
                .collect(Collectors.toList());
    }

    /**
     * Get events for an aggregate starting from a specific version.
     */
    public List<DomainEvent> getEventsFromVersion(String aggregateId, long fromVersion) {
        return eventsByAggregate.getOrDefault(aggregateId, List.of())
                .stream()
                .filter(e -> e.version() > fromVersion)
                .map(StoredEvent::event)
                .collect(Collectors.toList());
    }

    /**
     * Get the current version of an aggregate.
     */
    public long getCurrentVersion(String aggregateId) {
        List<StoredEvent> events = eventsByAggregate.get(aggregateId);
        return events == null || events.isEmpty() ? 0 : events.getLast().version();
    }

    /**
     * Get all events from the global stream (for projections).
     */
    public List<StoredEvent> getAllEvents() {
        return List.copyOf(globalEventStream);
    }

    /**
     * Get events from global stream starting at a position.
     */
    public List<StoredEvent> getEventsFromPosition(long fromPosition) {
        return globalEventStream.stream()
                .filter(e -> e.globalSequence() > fromPosition)
                .collect(Collectors.toList());
    }

    /**
     * Get all aggregate IDs of a specific type.
     */
    public Set<String> getAggregateIds(String aggregateType) {
        return eventsByAggregate.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() &&
                        e.getValue().getFirst().aggregateType().equals(aggregateType))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Register an event listener for real-time projections.
     */
    public void registerListener(EventListener listener) {
        listeners.add(listener);
        logger.info("Registered event listener: {}", listener.getClass().getSimpleName());
    }

    private void notifyListeners(StoredEvent event) {
        for (EventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                logger.error("Error notifying listener {}: {}",
                        listener.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * Get event count for metrics.
     */
    public long getEventCount() {
        return globalEventStream.size();
    }

    /**
     * Clear all events (for testing).
     */
    public void clear() {
        eventsByAggregate.clear();
        globalEventStream.clear();
        logger.info("Event store cleared");
    }

    /**
     * Record representing a stored event with metadata.
     */
    public record StoredEvent(
            long globalSequence,
            String eventId,
            String aggregateId,
            String aggregateType,
            String eventType,
            long version,
            java.time.Instant timestamp,
            DomainEvent event
    ) {}

    /**
     * Listener interface for real-time event processing.
     */
    public interface EventListener {
        void onEvent(StoredEvent event);
    }

    /**
     * Exception for optimistic concurrency conflicts.
     */
    public static class ConcurrencyException extends RuntimeException {
        public ConcurrencyException(String message) {
            super(message);
        }
    }
}

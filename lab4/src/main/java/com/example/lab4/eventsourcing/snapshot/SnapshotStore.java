package com.example.lab4.eventsourcing.snapshot;

import com.example.lab4.eventsourcing.event.DomainEvent;
import com.example.lab4.eventsourcing.event.PreferenceEvents.*;
import com.example.lab4.eventsourcing.store.EventStore;
import jakarta.ws.rs.core.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snapshot Store for Event Sourcing.
 * Snapshots capture the aggregate state at a point in time to avoid
 * replaying all events from the beginning.
 */
@Component
public class SnapshotStore {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotStore.class);

    // Threshold for creating snapshots (every N events)
    private static final int SNAPSHOT_THRESHOLD = 10;

    private final EventStore eventStore;

    // Snapshots by aggregateId
    private final Map<String, Snapshot> snapshots = new ConcurrentHashMap<>();

    // Track event counts for snapshot decisions
    private final Map<String, Integer> eventCountsSinceSnapshot = new ConcurrentHashMap<>();

    public SnapshotStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    /**
     * Get the latest snapshot for an aggregate, if one exists.
     */
    public Optional<Snapshot> getSnapshot(String aggregateId) {
        return Optional.ofNullable(snapshots.get(aggregateId));
    }

    /**
     * Create a snapshot of the current aggregate state.
     */
    public Snapshot createSnapshot(String aggregateId, PreferenceAggregate aggregate) {
        Snapshot snapshot = new Snapshot(
                aggregateId,
                aggregate.getStudentId(),
                aggregate.getCourseIds(),
                aggregate.getStatus(),
                aggregate.getVersion(),
                Instant.now()
        );

        snapshots.put(aggregateId, snapshot);
        eventCountsSinceSnapshot.put(aggregateId, 0);

        logger.info("Created snapshot for aggregate {} at version {}", aggregateId, aggregate.getVersion());
        return snapshot;
    }

    /**
     * Check if a snapshot should be created based on event count.
     */
    public boolean shouldCreateSnapshot(String aggregateId) {
        int count = eventCountsSinceSnapshot.getOrDefault(aggregateId, 0);
        return count >= SNAPSHOT_THRESHOLD;
    }

    /**
     * Increment the event count for an aggregate.
     */
    public void incrementEventCount(String aggregateId) {
        eventCountsSinceSnapshot.merge(aggregateId, 1, Integer::sum);
    }

    /**
     * Load an aggregate using snapshot + events replay.
     * This is more efficient than replaying all events from the beginning.
     */
    public PreferenceAggregate loadAggregate(String aggregateId) {
        PreferenceAggregate aggregate;

        // Try to load from snapshot first
        Optional<Snapshot> snapshotOpt = getSnapshot(aggregateId);
        if (snapshotOpt.isPresent()) {
            Snapshot snapshot = snapshotOpt.get();
            aggregate = new PreferenceAggregate(
                    aggregateId,
                    snapshot.studentId(),
                    snapshot.courseIds(),
                    snapshot.status(),
                    snapshot.version()
            );

            // Replay events since snapshot
            List<DomainEvent> eventsSinceSnapshot = eventStore.getEventsFromVersion(
                    aggregateId, snapshot.version());

            for (DomainEvent event : eventsSinceSnapshot) {
                aggregate.apply(event);
            }

            logger.debug("Loaded aggregate {} from snapshot (version {}) + {} events",
                    aggregateId, snapshot.version(), eventsSinceSnapshot.size());

        } else {
            // No snapshot, replay all events
            aggregate = new PreferenceAggregate(aggregateId);
            List<DomainEvent> allEvents = eventStore.getEvents(aggregateId);

            for (DomainEvent event : allEvents) {
                aggregate.apply(event);
            }

            logger.debug("Loaded aggregate {} by replaying {} events", aggregateId, allEvents.size());
        }

        return aggregate;
    }

    /**
     * Delete a snapshot (for testing or cleanup).
     */
    public void deleteSnapshot(String aggregateId) {
        snapshots.remove(aggregateId);
        eventCountsSinceSnapshot.remove(aggregateId);
        logger.info("Deleted snapshot for aggregate {}", aggregateId);
    }

    /**
     * Get all snapshots (for monitoring/debugging).
     */
    public Map<String, Snapshot> getAllSnapshots() {
        return Map.copyOf(snapshots);
    }

    /**
     * Snapshot record containing aggregate state at a point in time.
     */
    public record Snapshot(
            String aggregateId,
            Long studentId,
            Map<Long, Long> courseIds,
            String status,
            long version,
            Instant createdAt
    ) {}

    /**
     * Aggregate root for preferences - used for applying events.
     */
    public static class PreferenceAggregate {
        private final String aggregateId;
        private Long studentId;
        private LinkedHashMap<Long, Long> courseAndPacksIds;
        private String status;
        private long version;

        public PreferenceAggregate(String aggregateId) {
            this.aggregateId = aggregateId;
            this.courseAndPacksIds = new LinkedHashMap<>();
            this.status = "DRAFT";
            this.version = 0;
        }

        public PreferenceAggregate(String aggregateId, Long studentId,
                                   Map<Long, Long> courseAndPacksIds, String status, long version) {
            this.aggregateId = aggregateId;
            this.studentId = studentId;
            this.courseAndPacksIds = new LinkedHashMap<>(courseAndPacksIds);
            this.status = status;
            this.version = version;
        }

        public void apply(DomainEvent event) {
            switch (event) {
                case PreferenceCreated e -> {
                    this.studentId = e.getStudentId();;
                    this.courseAndPacksIds = new LinkedHashMap<>(e.getCourseIds());
                    this.status = "DRAFT";
                    this.version = e.getVersion();
                }
                case CourseAdded e -> {
                    LinkedHashMap<Long, Long> rebuilt = new LinkedHashMap<>(courseAndPacksIds.size() + 1);
                    boolean inserted = false;
                    int position = e.getPosition();
                    for (var entry : courseAndPacksIds.entrySet()) {

                        if (!inserted && java.util.Objects.equals(e.getPackId(), entry.getValue()) ) {
                            if (position == 0) {
                                rebuilt.put(e.getCourseId(), e.getPackId());
                                inserted = true;
                            } else position--;
                        }
                        rebuilt.put(entry.getKey(), entry.getValue());
                    }
                    if (!inserted) {
                        rebuilt.put(e.getCourseId(), e.getPackId());
                    }

                    courseAndPacksIds.clear();
                    courseAndPacksIds.putAll(rebuilt);
                    this.version = e.getVersion();
                }
                case CourseRemoved e -> {
                    courseAndPacksIds.remove(e.getCourseId());
                    this.version = e.getVersion();
                }
                case PreferencesReordered e -> {
                    this.courseAndPacksIds = new LinkedHashMap<>(e.getNewOrder());
                    this.version = e.getVersion();
                }
                case PreferencesSubmitted e -> {
                    this.courseAndPacksIds = new LinkedHashMap<>(e.getFinalOrder());
                    this.status = "SUBMITTED";
                    this.version = e.getVersion();
                }
                case PreferencesCleared e -> {
                    this.courseAndPacksIds.clear();
                    this.status = "DRAFT";
                    this.version = e.getVersion();
                }
                default -> { }
            }
        }

        // Getters
        public String getAggregateId() { return aggregateId; }
        public Long getStudentId() { return studentId; }
        public Map<Long,Long> getCourseIds() { return courseAndPacksIds; }
        public String getStatus() { return status; }
        public long getVersion() { return version; }
    }
}

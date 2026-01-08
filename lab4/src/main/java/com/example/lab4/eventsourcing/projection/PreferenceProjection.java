package com.example.lab4.eventsourcing.projection;

import com.example.lab4.eventsourcing.event.DomainEvent;
import com.example.lab4.eventsourcing.event.PreferenceEvents.*;
import com.example.lab4.eventsourcing.store.EventStore;
import com.example.lab4.eventsourcing.store.EventStore.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Projection that maintains the current state of student preferences.
 * Subscribes to events from the Event Store and updates its read model.
 * This implements the read-side of Event Sourcing with real-time updates.
 */
@Component
public class PreferenceProjection implements EventStore.EventListener {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceProjection.class);

    private final EventStore eventStore;

    // Read model: Current state of all preferences
    private final Map<String, PreferenceReadModel> preferences = new ConcurrentHashMap<>();

    // Track processed events for replay
    private long lastProcessedPosition = 0;

    public PreferenceProjection(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @PostConstruct
    public void initialize() {
        // Register for real-time updates
        eventStore.registerListener(this);

        // Rebuild projection from existing events
        rebuildFromEvents();

        logger.info("PreferenceProjection initialized with {} preferences", preferences.size());
    }

    /**
     * Handle incoming events in real-time.
     */
    @Override
    public void onEvent(StoredEvent storedEvent) {
        applyEvent(storedEvent.event());
        lastProcessedPosition = storedEvent.globalSequence();
    }

    /**
     * Rebuild the entire projection from event store.
     */
    public void rebuildFromEvents() {
        logger.info("Rebuilding preference projection from events...");
        preferences.clear();
        lastProcessedPosition = 0;

        List<StoredEvent> allEvents = eventStore.getAllEvents();
        for (StoredEvent storedEvent : allEvents) {
            if ("StudentPreference".equals(storedEvent.aggregateType())) {
                applyEvent(storedEvent.event());
                lastProcessedPosition = storedEvent.globalSequence();
            }
        }

        logger.info("Projection rebuilt: {} preferences from {} events",
                preferences.size(), lastProcessedPosition);
    }

    /**
     * Apply an event to update the read model.
     */
    private void applyEvent(DomainEvent event) {
        switch (event) {
            case PreferenceCreated e -> {
                PreferenceReadModel model = new PreferenceReadModel(
                        e.getAggregateId(),
                        e.getStudentId(),
                        new LinkedHashMap<>(e.getCourseIds()),
                        e.getVersion(),
                        e.getTimestamp(),
                        "DRAFT"
                );
                preferences.put(e.getAggregateId(), model);
                logger.debug("Created preference: {}", e.getAggregateId());
            }
            case CourseAdded e -> {
                PreferenceReadModel model = preferences.get(e.getAggregateId());
                if (model != null) {
                    model.addCourse(e.getCourseId(), e.getPosition(), e.getPackId());
                    model.updateVersion(e.getVersion(), e.getTimestamp());
                    logger.debug("Added course {} to preference {}", e.getCourseId(), e.getAggregateId());
                }
            }
            case CourseRemoved e -> {
                PreferenceReadModel model = preferences.get(e.getAggregateId());
                if (model != null) {
                    model.removeCourse(e.getCourseId());
                    model.updateVersion(e.getVersion(), e.getTimestamp());
                    logger.debug("Removed course {} from preference {}", e.getCourseId(), e.getAggregateId());
                }
            }
            case PreferencesReordered e -> {
                PreferenceReadModel model = preferences.get(e.getAggregateId());
                if (model != null) {
                    model.reorder(e.getNewOrder());
                    model.updateVersion(e.getVersion(), e.getTimestamp());
                    logger.debug("Reordered preference {}", e.getAggregateId());
                }
            }
            case PreferencesSubmitted e -> {
                PreferenceReadModel model = preferences.get(e.getAggregateId());
                if (model != null) {
                    model.submit(e.getFinalOrder());
                    model.updateVersion(e.getVersion(), e.getTimestamp());
                    logger.debug("Submitted preference {}", e.getAggregateId());
                }
            }
            case PreferencesCleared e -> {
                PreferenceReadModel model = preferences.get(e.getAggregateId());
                if (model != null) {
                    model.clear();
                    model.updateVersion(e.getVersion(), e.getTimestamp());
                    logger.debug("Cleared preference {}", e.getAggregateId());
                }
            }
            default -> logger.warn("Unknown event type: {}", event.getEventType());
        }
    }

    // Query methods for the read model

    public Optional<PreferenceReadModel> getPreference(String aggregateId) {
        return Optional.ofNullable(preferences.get(aggregateId));
    }

    public Optional<PreferenceReadModel> getPreferenceByStudent(Long studentId) {
        return preferences.values().stream()
                .filter(p -> p.getStudentId().equals(studentId))
                .findFirst();
    }

    public List<PreferenceReadModel> getPreferencesByPack(Long packId) {
        return preferences.values().stream()
                .filter(p -> p.courseIdandPacksId.entrySet().stream().anyMatch(e -> e.getValue().equals(packId)))
                .toList();
    }

    public List<PreferenceReadModel> getSubmittedPreferences() {
        return preferences.values().stream()
                .filter(p -> "SUBMITTED".equals(p.getStatus()))
                .toList();
    }

    public List<PreferenceReadModel> getAllPreferences() {
        return new ArrayList<>(preferences.values());
    }

    public int getPreferenceCount() {
        return preferences.size();
    }

    /**
     * Read model representing the current state of a student's preferences.
     */
    public static class PreferenceReadModel {
        private final String aggregateId;
        private final Long studentId;
        private final LinkedHashMap<Long,Long> courseIdandPacksId;
        private long version;
        private Instant lastUpdated;
        private String status; // DRAFT, SUBMITTED

        public PreferenceReadModel(String aggregateId, Long studentId,
                                   LinkedHashMap<Long, Long> courseIdandPacksId, long version, Instant lastUpdated, String status) {
            this.aggregateId = aggregateId;
            this.studentId = studentId;
            this.courseIdandPacksId = courseIdandPacksId;
            this.version = version;
            this.lastUpdated = lastUpdated;
            this.status = status;
        }


        public void addCourse(Long courseId,  int position, Long packId) {

            LinkedHashMap<Long, Long> rebuilt = new LinkedHashMap<>(courseIdandPacksId.size() + 1);
            boolean inserted = false;

            for (var e : courseIdandPacksId.entrySet()) {

                if (!inserted && java.util.Objects.equals(e.getValue(), packId) ) {
                    if (position == 0) {
                        rebuilt.put(courseId, packId);
                        inserted = true;
                    } else position--;
                }
                rebuilt.put(e.getKey(), e.getValue());
            }
            if (!inserted) {
                rebuilt.put(courseId, packId);
            }

            courseIdandPacksId.clear();
            courseIdandPacksId.putAll(rebuilt);
        }

        public void removeCourse(Long courseId) {
            courseIdandPacksId.remove(courseId);
        }

        public void reorder(Map<Long, Long> newOrder) {

            courseIdandPacksId.clear();
            courseIdandPacksId.putAll(newOrder);
        }

        public void submit(Map<Long,Long> finalOrder) {
            courseIdandPacksId.clear();
            courseIdandPacksId.putAll(finalOrder);
            status = "SUBMITTED";
        }

        public void clear() {
            courseIdandPacksId.clear();
            status = "DRAFT";
        }

        public void updateVersion(long version, Instant timestamp) {
            this.version = version;
            this.lastUpdated = timestamp;
        }

        // Getters
        public String getAggregateId() { return aggregateId; }
        public Long getStudentId() { return studentId; }
        public LinkedHashMap<Long, Long> getCourseAndPacksId() { return courseIdandPacksId; }
        public long getVersion() { return version; }
        public Instant getLastUpdated() { return lastUpdated; }
        public String getStatus() { return status; }
    }
}

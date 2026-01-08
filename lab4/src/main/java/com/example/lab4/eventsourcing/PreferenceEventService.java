package com.example.lab4.eventsourcing;

import com.example.lab4.eventsourcing.event.DomainEvent;
import com.example.lab4.eventsourcing.event.PreferenceEvents.*;
import com.example.lab4.eventsourcing.projection.PreferenceProjection;
import com.example.lab4.eventsourcing.projection.PreferenceProjection.PreferenceReadModel;
import com.example.lab4.eventsourcing.snapshot.SnapshotStore;
import com.example.lab4.eventsourcing.snapshot.SnapshotStore.PreferenceAggregate;
import com.example.lab4.eventsourcing.store.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that implements Event Sourcing for student preferences.
 * Coordinates between Event Store, Projections, and Snapshots.
 */
@Service
public class PreferenceEventService {

    private static final Logger logger = LoggerFactory.getLogger(PreferenceEventService.class);

    private final EventStore eventStore;
    private final PreferenceProjection projection;
    private final SnapshotStore snapshotStore;

    public PreferenceEventService(EventStore eventStore, PreferenceProjection projection,
                                  SnapshotStore snapshotStore) {
        this.eventStore = eventStore;
        this.projection = projection;
        this.snapshotStore = snapshotStore;
    }

    // =====================================================
    // Command Methods (Write Side)
    // =====================================================

    /**
     * Create a new preference list for a student.
     */
    public String createPreference(Long studentId, Map<Long, Long> courseIds) {
        String aggregateId = "pref-" + UUID.randomUUID().toString().substring(0, 8);

        PreferenceCreated event = new PreferenceCreated(
                aggregateId, 1, studentId, courseIds);

        eventStore.appendEvents(aggregateId, List.of(event), 0);
        logger.info("Created preference {} for student {} with {} courses",
                aggregateId, studentId, courseIds.size());

        return aggregateId;
    }

    /**
     * Add a course to an existing preference list.
     */
    public void addCourse(String aggregateId, Long courseId, int position, Long packId) {
        long currentVersion = eventStore.getCurrentVersion(aggregateId);

        CourseAdded event = new CourseAdded(aggregateId, currentVersion + 1, courseId, position, packId);
        eventStore.appendEvents(aggregateId, List.of(event), currentVersion);

        checkAndCreateSnapshot(aggregateId);
        logger.info("Added course {} to preference {} at position {} for pack {}", courseId, aggregateId, position, packId);
    }

    /**
     * Remove a course from the preference list.
     */
    public void removeCourse(String aggregateId, Long courseId) {
        long currentVersion = eventStore.getCurrentVersion(aggregateId);

        CourseRemoved event = new CourseRemoved(aggregateId, currentVersion + 1, courseId);
        eventStore.appendEvents(aggregateId, List.of(event), currentVersion);

        checkAndCreateSnapshot(aggregateId);
        logger.info("Removed course {} from preference {}", courseId, aggregateId);
    }

    /**
     * Reorder the preference list.
     */
    public void reorderPreferences(String aggregateId, Map<Long, Long> newOrder) {
        long currentVersion = eventStore.getCurrentVersion(aggregateId);

        PreferencesReordered event = new PreferencesReordered(aggregateId, currentVersion + 1, newOrder);
        eventStore.appendEvents(aggregateId, List.of(event), currentVersion);

        checkAndCreateSnapshot(aggregateId);
        logger.info("Reordered preference {} with {} courses", aggregateId, newOrder.size());
    }

    /**
     * Submit (finalize) the preferences.
     */
    public void submitPreferences(String aggregateId) {
        // Load current state to get the final order
        PreferenceAggregate aggregate = snapshotStore.loadAggregate(aggregateId);
        long currentVersion = aggregate.getVersion();

        PreferencesSubmitted event = new PreferencesSubmitted(
                aggregateId, currentVersion + 1, aggregate.getCourseIds());
        eventStore.appendEvents(aggregateId, List.of(event), currentVersion);

        // Always create snapshot on submit
        snapshotStore.createSnapshot(aggregateId, snapshotStore.loadAggregate(aggregateId));
        logger.info("Submitted preference {} with {} courses", aggregateId, aggregate.getCourseIds().size());
    }

    /**
     * Clear all preferences for an aggregate.
     */
    public void clearPreferences(String aggregateId, String reason) {
        long currentVersion = eventStore.getCurrentVersion(aggregateId);

        PreferencesCleared event = new PreferencesCleared(aggregateId, currentVersion + 1, reason);
        eventStore.appendEvents(aggregateId, List.of(event), currentVersion);

        checkAndCreateSnapshot(aggregateId);
        logger.info("Cleared preference {}: {}", aggregateId, reason);
    }

    // =====================================================
    // Query Methods (Read Side - via Projection)
    // =====================================================

    /**
     * Get preference by aggregate ID.
     */
    public Optional<PreferenceReadModel> getPreference(String aggregateId) {
        return projection.getPreference(aggregateId);
    }

    /**
     * Get preference by student ID.
     */
    public Optional<PreferenceReadModel> getPreferenceByStudent(Long studentId) {
        return projection.getPreferenceByStudent(studentId);
    }

    /**
     * Get all preferences for a pack.
     */
    public List<PreferenceReadModel> getPreferencesByPack(Long packId) {
        return projection.getPreferencesByPack(packId);
    }

    /**
     * Get all submitted preferences.
     */
    public List<PreferenceReadModel> getSubmittedPreferences() {
        return projection.getSubmittedPreferences();
    }

    /**
     * Get all preferences.
     */
    public List<PreferenceReadModel> getAllPreferences() {
        return projection.getAllPreferences();
    }

    // =====================================================
    // Event History Methods
    // =====================================================

    /**
     * Get the full event history for a preference.
     */
    public List<DomainEvent> getEventHistory(String aggregateId) {
        return eventStore.getEvents(aggregateId);
    }

    /**
     * Rebuild the aggregate from events (time travel).
     */
    public PreferenceAggregate rebuildAggregate(String aggregateId) {
        return snapshotStore.loadAggregate(aggregateId);
    }

    /**
     * Rebuild the projection from all events.
     */
    public void rebuildProjection() {
        projection.rebuildFromEvents();
    }

    // =====================================================
    // Snapshot Management
    // =====================================================

    private void checkAndCreateSnapshot(String aggregateId) {
        snapshotStore.incrementEventCount(aggregateId);

        if (snapshotStore.shouldCreateSnapshot(aggregateId)) {
            PreferenceAggregate aggregate = snapshotStore.loadAggregate(aggregateId);
            snapshotStore.createSnapshot(aggregateId, aggregate);
        }
    }

    /**
     * Get snapshot for an aggregate.
     */
    public Optional<SnapshotStore.Snapshot> getSnapshot(String aggregateId) {
        return snapshotStore.getSnapshot(aggregateId);
    }

    // =====================================================
    // Metrics
    // =====================================================

    public long getTotalEventCount() {
        return eventStore.getEventCount();
    }

    public int getPreferenceCount() {
        return projection.getPreferenceCount();
    }

    public int getSnapshotCount() {
        return snapshotStore.getAllSnapshots().size();
    }
}

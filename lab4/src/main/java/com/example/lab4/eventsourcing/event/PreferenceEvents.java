package com.example.lab4.eventsourcing.event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Domain events for Student Preferences aggregate.
 * These events capture all state changes for student course preferences.
 */
public class PreferenceEvents {

    private static final String AGGREGATE_TYPE = "StudentPreference";

    /**
     * Event: A new preference list was created for a student
     */
    public static class PreferenceCreated extends DomainEvent {
        private final Long studentId;
        private final LinkedHashMap<Long, Long> courseIdsAndPacks;

        public PreferenceCreated(String aggregateId, long version, Long studentId,
                                 Map<Long, Long> courseIdsAndPacks) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.studentId = studentId;
            this.courseIdsAndPacks = new LinkedHashMap<Long, Long>(courseIdsAndPacks);
        }

        public Long getStudentId() { return studentId; }
        public LinkedHashMap<Long, Long> getCourseIds() { return courseIdsAndPacks; }
    }

    /**
     * Event: A course was added to the preference list
     */
    public static class CourseAdded extends DomainEvent {
        private final Long courseId;
        private final Long packId;
        private final int position;

        public CourseAdded(String aggregateId, long version, Long courseId, int position) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.courseId = courseId;
            this.position = position;
            this.packId = null;
        }


        public CourseAdded(String aggregateId, long version, Long courseId, int position, Long packId) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.courseId = courseId;
            this.position = position;
            this.packId = packId;
        }

        public Long getCourseId() { return courseId; }
        public int getPosition() { return position; }
        public Long getPackId() { return packId; }
    }

    /**
     * Event: A course was removed from the preference list
     */
    public static class CourseRemoved extends DomainEvent {
        private final Long courseId;

        public CourseRemoved(String aggregateId, long version, Long courseId) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.courseId = courseId;
        }

        public Long getCourseId() { return courseId; }
    }

    /**
     * Event: Course preferences were reordered
     */
    public static class PreferencesReordered extends DomainEvent {
        private final Map<Long, Long> newOrder;

        public PreferencesReordered(String aggregateId, long version,  Map<Long, Long> newOrder) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.newOrder = Map.copyOf(newOrder);
        }

        public  Map<Long, Long> getNewOrder() { return newOrder; }
    }

    /**
     * Event: Preferences were submitted (finalized)
     */
    public static class PreferencesSubmitted extends DomainEvent {
        private final Map<Long, Long> finalOrder;

        public PreferencesSubmitted(String aggregateId, long version,  Map<Long, Long> finalOrder) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.finalOrder = Map.copyOf(finalOrder);
        }

        public Map<Long, Long> getFinalOrder() { return finalOrder; }
    }

    /**
     * Event: Preferences were cleared/reset
     */
    public static class PreferencesCleared extends DomainEvent {
        private final String reason;

        public PreferencesCleared(String aggregateId, long version, String reason) {
            super(aggregateId, AGGREGATE_TYPE, version);
            this.reason = reason;
        }

        public String getReason() { return reason; }
    }
}

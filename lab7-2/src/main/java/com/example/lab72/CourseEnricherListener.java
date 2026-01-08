package com.example.lab72;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CourseEnricherListener {
    Logger logger = LoggerFactory.getLogger(CourseEnricherListener.class);

    private final KafkaTemplate<String, GradeEventFinal> kafkaTemplate;
    private final CourseRepository courseService;

    public CourseEnricherListener( KafkaTemplate<String, GradeEventFinal> kafkaTemplate,
                                  CourseRepository courseService) {
        this.kafkaTemplate = kafkaTemplate;
        this.courseService = courseService;
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true",
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = "grades.student-enriched",
            groupId = "grades-full",
            containerFactory = "gradeEventStudentKafkaListenerContainerFactory"
    )
    public void enrichCourse(GradeEventStudent event) {
        Course c = courseService.findByCode(event.getCourseCode());

        GradeEventFinal gradeEventFinal = new GradeEventFinal(event);

        gradeEventFinal.setCourseId(c.getId());
        gradeEventFinal.setCourseName(c.getName());
        gradeEventFinal.setSemester(c.getPack() != null ? c.getPack().getSemester() : 1);

        logger.info("Enrichment of student with code {} and name {}", event.getStudentCode(), event.getStudentName());
        kafkaTemplate.send("grades.full",
                event.getStudentCode(),
                gradeEventFinal);
    }
}

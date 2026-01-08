package com.example.lab7;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class GradeEventPublisher {

    private final KafkaTemplate<String, GradeEvent> kafkaTemplate;
    private final String gradesTopic;

    public GradeEventPublisher(
            KafkaTemplate<String, GradeEvent> kafkaTemplate,
            @Value("${app.topics.grades}") String gradesTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.gradesTopic = gradesTopic;
    }

    public void publishGradeAssigned(String studentCode, String courseCode, java.math.BigDecimal grade) {
        GradeEvent event = new GradeEvent(studentCode, courseCode, grade);
        kafkaTemplate.send(gradesTopic, studentCode, event);
    }

}

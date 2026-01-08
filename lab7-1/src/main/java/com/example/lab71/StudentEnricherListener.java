package com.example.lab71;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@EnableKafka
public class StudentEnricherListener {
    private final KafkaTemplate<String, GradeEventStudent> kafkaTemplate;
    private final StudentRepository studentService; // your own service or repo
    Logger logger = LoggerFactory.getLogger(StudentEnricherListener.class);

    public StudentEnricherListener( KafkaTemplate<String, GradeEventStudent> kafkaTemplate,
                                   StudentRepository studentService) {
        this.kafkaTemplate = kafkaTemplate;
        this.studentService = studentService;
    }


    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true",
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = "grades.basic",
            groupId = "student-enricher",
            containerFactory = "gradeEventKafkaListenerContainerFactory" // if it consumes GradeEvent
    )
    public void enrichStudent(GradeEvent event) {
        Student s = studentService.findByCode(event.getStudentCode());
        GradeEventStudent gradeEventStudent = new GradeEventStudent(event);

        gradeEventStudent.setStudentId(s.getId());
        gradeEventStudent.setStudentName(s.getName());
        gradeEventStudent.setStudentYear(s.getYear());

        logger.info("Enrichment of student with code {} and name {}", gradeEventStudent.getStudentCode(), gradeEventStudent.getStudentName());
        kafkaTemplate.send("grades.student-enriched",
                event.getStudentCode(),
                gradeEventStudent);
    }

}


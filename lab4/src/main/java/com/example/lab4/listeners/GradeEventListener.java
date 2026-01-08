package com.example.lab4.listeners;

import com.example.lab4.dto.GradeEvent;
import com.example.lab4.service.StudentGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;


@Service
public class GradeEventListener {

    Logger logger = LoggerFactory.getLogger(GradeEventListener.class);
    @Autowired
    StudentGradeService studentGradeService;


    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true",
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(
            topics = "grades.full",
            groupId = "grades-full",
            containerFactory = "gradeEventFinalKafkaListenerContainerFactory"
    )
    public void handleGradeAssigned(GradeEvent event) {
        System.out.printf(
                "PrefSchedule received grade event: student=%s, course=%s, grade=%s%n",
                event.getStudentCode(),
                event.getCourseCode(),
                event.getGrade()
        );

        studentGradeService.handleGradeEvent(event);

    }



    @DltHandler
    public void handleDlt(GradeEvent event,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          Headers headers) {

        logger.error("Message routed to DLQ topic={}, payload={}", topic, event);
    }
}
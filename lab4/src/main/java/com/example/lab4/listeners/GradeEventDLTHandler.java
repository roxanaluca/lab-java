package com.example.lab4.listeners;

import com.example.lab4.dto.GradeEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class GradeEventDLTHandler {


    @KafkaListener(
            topics = "grades.full-dlt",
            groupId = "grades-full-dlt",
            containerFactory = "gradeEventFinalKafkaListenerContainerFactory"
    )
    public void handleDeadLetter(
            GradeEvent event,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
            @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionType) {

        System.err.printf(
                "DLT received event from " +
                        "exception=%s (%s); event=%s%n",
                exceptionMessage, exceptionType,
                event
        );
    }
}

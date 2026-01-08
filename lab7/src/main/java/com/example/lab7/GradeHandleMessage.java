package com.example.lab7;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller

public class GradeHandleMessage {
        @Autowired
        GradeEventPublisher publisher;

        @MessageMapping("/grade") // listens to /app/chat
        @SendTo("/topic/messages") // broadcasts to /topic/messages
        public String sendMessage(GradeRequest message) {
            System.out.println("Received: " + message);
            publisher.publishGradeAssigned(
                    message.getStudentCode(),
                    message.getCourseCode(),
                    message.getGrade()
            );
            return "success";
        }
}

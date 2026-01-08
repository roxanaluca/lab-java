package com.example.lab4.service;

import com.example.lab4.dto.GradeEvent;
import com.example.lab4.repository.GradeRepository;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CSVGradeService {
//
//    @Autowired
//    @Qualifier("quickGrade")
//    WebClient webClient;

    public CSVGradeService(WebSocketStompClient stompClient) {
        this.stompClient = stompClient;
    }

    final private WebSocketStompClient stompClient;
    private StompSession session;

    @Value("${quickgrade.base-url}")
    private String quickGradeWsUrl;

    @Value("${quickgrade.connect:true}")
    private boolean connectOnStartup;

    @PostConstruct
    public void connect() throws Exception {
        if (!connectOnStartup) {
            return;
        }
        var future = stompClient.connectAsync(quickGradeWsUrl, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                System.out.println("Connected to QuickGrade WS");
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                System.err.println("Transport error to QuickGrade WS: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        this.session = future.get(5, TimeUnit.SECONDS);
    }


    public String processFile(MultipartFile file) {


        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        int processed = 0;
        int skipped = 0;

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));

            String line;
            boolean isFirstLine = true;
            int lineNo = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Basic check for header row
                if (isFirstLine && line.toLowerCase().contains("student") && line.toLowerCase().contains("course")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;

                String[] parts = line.split(",");
                if (parts.length < 3) {
                    skipped++;
                    continue;
                }

                String studentCode = parts[0].trim();
                String courseCode = parts[1].trim();
                String gradeStr = parts[2].trim();

                lineNo++;
                try {
                    BigDecimal gradeValue = new BigDecimal(gradeStr);
                    GradeEvent gradeEvent = new GradeEvent(studentCode, courseCode, gradeValue);

                    session.send("/app/grade", gradeEvent);

                    processed++;
                } catch (NumberFormatException e) {
                    skipped++;
                }
            }
        } catch (Exception e) {

        }

        return String.format("CSV processed. Imported: %d, skipped: %d", processed, skipped);
    }
}

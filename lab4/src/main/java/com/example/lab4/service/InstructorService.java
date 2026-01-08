package com.example.lab4.service;

import com.example.lab4.dto.preference.Algorithm;
import com.example.lab4.dto.preference.CourseDta;
import com.example.lab4.dto.preference.SolveMatchingDta;
import com.example.lab4.dto.preference.StudentDta;
import com.example.lab4.entity.Grade;
import com.example.lab4.entity.InstructorPreferenceItem;
import com.example.lab4.entity.Pack;
import com.example.lab4.entity.StudentPreferenceItem;
import com.example.lab4.errors.PackTimeout;
import com.example.lab4.repository.GradeRepository;
import com.example.lab4.repository.InstructorPreferenceItemRepository;
import com.example.lab4.repository.InstructorRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentPreferenceItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Fallback;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class InstructorService {


    private final Counter fallbackCounter;

    private final WebClient client;

    public InstructorService( @Value("${stablematch.base-url}")String urlPath, MeterRegistry meterRegistry){
        this.fallbackCounter = Counter.builder("stablematch.fallback.invocations")
                .description("Number of fallback invocations due to failures")
                .tag("service", "stablematch")
                .register(meterRegistry);
        client = WebClient.builder()
                .baseUrl(urlPath)
                .build();

    }

    private static final Logger logger = LoggerFactory.getLogger(InstructorService.class);

    @Autowired
    private InstructorPreferenceItemRepository instructorPreferenceItemRepository;

    @Autowired
    private StudentPreferenceItemRepository studentPreferenceItemRepository;

    @Autowired
    private PackRepository packRepository;

    @Autowired
    private GradeRepository gradeRepository;

    final private ObjectMapper objectMapper = new ObjectMapper();

    private SolveMatchingDta buildMatchingRequest(Long packId, Algorithm algorithm) {
        List<InstructorPreferenceItem> instructorPreferenceItems = instructorPreferenceItemRepository.findAllByPackId(packId);
        List<StudentPreferenceItem> studentPreferenceItems = studentPreferenceItemRepository.findAllByPackId(packId);

        studentPreferenceItems.sort(new Comparator<StudentPreferenceItem>() {
            @Override
            public int compare(StudentPreferenceItem o1, StudentPreferenceItem o2) {
                int result = Math.toIntExact(o1.getStudentPreference().getStudent().getId() - o2.getStudentPreference().getStudent().getId());
                if (result != 0)
                    return result;

                return (int) Math.floor(o1.getRank() - o2.getRank());
            }
        });

        instructorPreferenceItems.sort(
                new Comparator<InstructorPreferenceItem>() {
                    @Override
                    public int compare(InstructorPreferenceItem o1, InstructorPreferenceItem o2) {
                        if (o1.equals(o2))
                            return Math.toIntExact(o1.getWeightPercent() - o2.getWeightPercent());
                        return o1.getId().compareTo(o2.getId());
                    }
                }
        );

        List<StudentDta> studentDtaList = new ArrayList<>();
        StudentDta studentDta = new StudentDta("", new ArrayList<>());
        HashSet<String> courseCodes = new HashSet<>();
        List<CourseDta> courseDta = new ArrayList<>();


        for (StudentPreferenceItem studentPreferenceItem : studentPreferenceItems) {
            if (!studentPreferenceItem.getStudentPreference().getStudent().getCode().equals(studentDta.studentId())) {
                if (!studentDta.studentId().isEmpty())
                    studentDtaList.add(studentDta);
                studentDta = new StudentDta(studentPreferenceItem.getStudentPreference().getStudent().getCode(), new ArrayList<>());
            }
            studentDta.preferences().add(studentPreferenceItem.getCourse().getCode());
            if (!courseCodes.contains(studentPreferenceItem.getCourse().getCode())) {
                courseCodes.add(studentPreferenceItem.getCourse().getCode());
                courseDta.add(new CourseDta(studentPreferenceItem.getCourse().getCode(), studentPreferenceItem.getCourse().getGroupCount()));
            }


        }
        studentDtaList.add(studentDta);
        Map<String, Map<String, Double>> studentScores = new HashMap<>();
        for (String course : courseCodes) {

            studentScores.put(course, new HashMap<>());
            for (StudentDta student : studentDtaList) {
                double weightedScore = 0.0;
                double totalWeight = 0.0;
                for (InstructorPreferenceItem pref : instructorPreferenceItems) {
                    List<Grade> grades = gradeRepository.findByStudentCode(student.studentId());
                    Optional<Grade> grade = grades.stream()
                            .filter(g -> g.getCourse() != null &&
                                    pref.getCourse().getCode().equals(g.getCourse().getCode()))
                            .findFirst();

                    if (grade.isPresent() && grade.get().getGrade() != null) {
                        double weight = pref.getWeightPercent() / 100.0;
                        weightedScore += grade.get().getGrade().doubleValue() * weight;
                        totalWeight += weight;
                    }
                }
                double finalScore = totalWeight > 0 ? weightedScore / totalWeight : 0.0;
                studentScores.get(course).put(student.studentId(), finalScore);
            }
        }


        Map<String, List<String>> rankings =
            studentScores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().entrySet().stream()
                                .sorted(
                                        Map.Entry.<String, Double>comparingByValue()
                                                .reversed()
                                                .thenComparing(Map.Entry::getKey) // tie-break
                                )
                                .map(Map.Entry::getKey) // studentId
                                .collect(Collectors.toList())
                ));


        SolveMatchingDta httpRequest = new SolveMatchingDta(
                packId.toString(), algorithm, studentDtaList, courseDta, rankings
        );
        return httpRequest;
    }


    @CircuitBreaker(name = "stableMatchClient", fallbackMethod = "solveFallback")
    @Retry(name = "stableMatchClient")
    @RateLimiter(name = "stableMatchClient")
    @Bulkhead(name = "stableMatchClient")
    public String matchPreference(Long packId, String algoritm)   throws PackTimeout {
//        logger.info("Solving matching for pack {} using algorithm={}", packId, algoritm);
        SolveMatchingDta httpRequest = buildMatchingRequest(packId, "RANDOM".equals(algoritm) ? Algorithm.RANDOM : Algorithm.GALE_SHAPLEY);
//        try {
//            String json = objectMapper.writeValueAsString(httpRequest);
//            System.out.println("httpRequest: " + json);
//        } catch (Exception e) {
//            throw new PackTimeout(packId);
//        }


        String result = client.post()
                .uri("/api/matching/solve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(httpRequest) // Spring will serialize to JSON
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("result: " + result);
        return result;
    }

    @TimeLimiter(name = "stableMatchClient", fallbackMethod = "solveAsyncFallback")
    public CompletableFuture<String> matchPreferenceAsync(Long packId, String algorithm) {
        return CompletableFuture.supplyAsync(() -> matchPreference(packId, algorithm));
    }


    public String solveFallback(Long packId, String algoritm, Throwable t)   throws PackTimeout {
        logger.warn("Fallback triggered for solveForPack: packId={}, error={}", packId, t.getMessage());
        fallbackCounter.count();
        throw new PackTimeout(packId);
    }

    public String matchPreferenceForAllPacks(String alg) {
        logger.info("Solving matching for all packs algorithm={}", alg);
        List<Long> packs = packRepository.findAll().stream().map(pack -> pack.getId()).collect(Collectors.toList());
        StringBuilder results = new StringBuilder();
        for (Long packId : packs) {
            String result = matchPreference(packId, alg);
            results.append(result).append("\n");
        }
        return results.toString();
    }
}

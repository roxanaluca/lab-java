package com.example.lab8.service;

import com.example.lab8.dta.SolveMatchingDta;
import com.example.lab8.dto.AssignmentDto;
import com.example.lab8.dto.SolveMatchingDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatchingService {

    @Autowired
    GaleShapleyMatching galeShapleyMatching;
    @Autowired
    RandomMatching randomMatching;

    private final InMemoryMatchingStore store;

    private final MeterRegistry meterRegistry;
    private static final Logger log = LoggerFactory.getLogger(GaleShapleyMatching.class);
    private final Counter solveInvocationCounter;
    private final Counter randomAlgorithmCounter;
    private final Counter galeShapleyAlgorithmCounter;

    private final Timer solveTimer;
    private final Timer randomAlgorithmTimer;
    private final Timer galeShapleyAlgorithmTimer;

    public MatchingService(InMemoryMatchingStore store,
                           MeterRegistry meterRegistry) {
        this.store = store;
        this.meterRegistry = meterRegistry;
        this.solveInvocationCounter = Counter.builder("stablematch.solve.invocations")
                .description("Total number of stable match solve invocations")
                .tag("service", "stablematch")
                .register(meterRegistry);

        this.randomAlgorithmCounter = Counter.builder("stablematch.algorithm.invocations")
                .description("Number of random algorithm invocations")
                .tag("algorithm", "random")
                .tag("service", "stablematch")
                .register(meterRegistry);

        this.galeShapleyAlgorithmCounter = Counter.builder("stablematch.algorithm.invocations")
                .description("Number of Gale-Shapley algorithm invocations")
                .tag("algorithm", "gale-shapley")
                .tag("service", "stablematch")
                .register(meterRegistry);


        // Create timers for response time measurement
        this.solveTimer = Timer.builder("stablematch.solve.time")
                .description("Time taken to solve stable match problem")
                .tag("service", "stablematch")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(meterRegistry);

        this.randomAlgorithmTimer = Timer.builder("stablematch.algorithm.time")
                .description("Time taken by random algorithm")
                .tag("algorithm", "random")
                .tag("service", "stablematch")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.galeShapleyAlgorithmTimer = Timer.builder("stablematch.algorithm.time")
                .description("Time taken by Gale-Shapley algorithm")
                .tag("algorithm", "gale-shapley")
                .tag("service", "stablematch")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

    }

    public SolveMatchingDto solve(SolveMatchingDta req) {
        log.info("Solving stable match problem with algorithm: {}, students: {}, courses: {}",
                req.algorithm(),
                req.students() != null ? req.students().size() : 0,
                req.courses() != null ? req.courses().size() : 0);

        // Increment invocation counter
        solveInvocationCounter.increment();
        return solveTimer.record(() ->
        {
            SolveMatchingDto response;
            switch (req.algorithm()) {
                case RANDOM: {
                    randomAlgorithmCounter.increment();
                    log.info("Using random matching");
                    response = randomAlgorithmTimer.record(() -> randomMatching.solveMatching(req));
                    break;
                }
                case GALE_SHAPLEY: {
                    log.info("Using gale shapley matching");
                    galeShapleyAlgorithmCounter.increment();
                    response = galeShapleyAlgorithmTimer.record(() -> galeShapleyMatching.solveMatching(req));
                    break;
                }
                default: {
                    log.info("No algorithm found");
                    throw new NoSuchElementException("No algorithm found");
                }
            }
            store.put(response);
            return response;
        });
    }


    public SolveMatchingDto getByIdOrThrow(String matchingId) {
        return store.get(matchingId).orElseThrow(() ->
                new NoSuchElementException("No matching with id=" + matchingId));
    }

    public Optional<AssignmentDto> getAssignmentForStudent(String matchingId, String studentId) {
        return getByIdOrThrow(matchingId).getAssignments().stream()
                .filter(a -> a.studentId().equals(studentId))
                .findFirst();
    }

    public List<String> getStudentsForCourse(String matchingId, String courseId) {
        return getByIdOrThrow(matchingId).getAssignments().stream()
                .filter(a -> a.courseId().equals(courseId))
                .map(AssignmentDto::studentId)
                .toList();
    }

    public List<SolveMatchingDto> listAll() {
        return store.listAll();
    }
}
package com.example.lab4;

import com.example.lab4.dto.GradeEvent;
import com.example.lab4.entity.Course;
import com.example.lab4.entity.Instructor;
import com.example.lab4.entity.Pack;
import com.example.lab4.repository.CourseRepository;
import com.example.lab4.repository.CourseRepositoryImp;
import com.example.lab4.repository.InstructorRepository;
import com.example.lab4.repository.PackRepository;
import com.example.lab4.repository.StudentRepository;
import com.example.lab4.service.PackService;
import com.example.lab4.webfilter.DBUserDetailsService;
import com.example.lab4.webfilter.JwtAuthenticationFilter;
import com.example.lab4.webfilter.JwtService;
import com.github.javafaker.Faker;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@Configuration
@EnableCaching
@EnableKafka
public class Lab4Config {
    private HikariConfig getDatasourceConfigFile(DBProperties dbProperties, String databaseName) {
        HikariConfig ds = new HikariConfig();
        ds.setJdbcUrl(dbProperties.url() + "/" + databaseName);
        ds.setUsername(dbProperties.username());
        ds.setPassword(dbProperties.password());
        ds.setDriverClassName(dbProperties.driverClassName());

        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(2);
        ds.setConnectionTestQuery("SELECT 1");

        return ds;
    }


    final private KafkaProperties kafkaProperties;

    public Lab4Config(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    @Primary
    public DataSource getDataSource(DBProperties dbProperties) {
        return new HikariDataSource(getDatasourceConfigFile(dbProperties, "appdb"));
    }

    //    @Bean
//    @Order(1)
    CommandLineRunner testDatabase(
            InstructorRepository instructorRepo,
            PackRepository packRepo,
            CourseRepository courseRepo
    ) {
        return args -> {
            System.out.println(">>> Running CommandLineRunner test...");

            Instructor instructor = new Instructor();
            instructor.setName("Alice Doe");
            instructor.setEmail("alice.doe@example.com");
            instructorRepo.save(instructor);

            Pack pack = new Pack();
            pack.setYear(2025);
            pack.setSemester(2);
            pack.setName("Autumn 2025 Courses");
            packRepo.save(pack);

            Course course = new Course();
            course.setType("Lab");
            course.setCode("CS102");
            course.setAbbr("CS");
            course.setName("Programming Fundamentals");
            course.setInstructor(instructor);
            course.setPack(pack);
            course.setGroupCount(2);
            course.setDescription("Hands-on programming course.");
            courseRepo.save(course);

            System.out.println(">>> Test data created successfully!");

            instructorRepo.delete(instructor);

            System.out.println(">>> Testing removing an instructor...!");
            assert course.getInstructor() == null;
            assert course.getPack().equals(pack);

            System.out.println(">>> Testing removing a pack...!");

            packRepo.delete(pack);
            assert course.getPack() == null;

            System.out.println(">>> Testing removing a course...!");
            courseRepo.delete(course);

        };
    }

    @Bean
    @Primary
    CourseRepositoryImp courseRepositoryImp() {
        return new CourseRepositoryImp();
    }

    //    @Bean
//    @Order(2)
    CommandLineRunner loadTestData(
            InstructorRepository instructorRepo,
            PackRepository packRepo,
            PackService packService,
            CourseRepository courseRepo,
            CourseRepositoryImp courseRepoImp
    ) {
        return args -> {
            Random random = new Random();
            Faker faker = new Faker(new Locale("en"));

            System.out.println("=== Populating database with fake data ===");

            Instructor instructor1 = new Instructor();
            instructor1.setName(faker.name().fullName());
            String email = faker.internet().emailAddress();
            while (instructorRepo.findByEmail(email) != null) {
                email = faker.internet().emailAddress();
            }
            instructor1.setEmail(email);
            instructorRepo.save(instructor1);

            Instructor instructor2 = new Instructor();
            instructor2.setName(faker.name().fullName());
            while (instructorRepo.findByEmail(email) != null) {
                email = faker.internet().emailAddress();
            }
            instructor2.setEmail(email);
            instructorRepo.save(instructor2);

            Pack pack1, pack2;
            if (packRepo.count() > 0) {
                pack1 = new Pack();
                pack1.setYear(2025);
                pack1.setSemester(1);
                pack1.setName("Spring 2025");
                packRepo.save(pack1);

                pack2 = new Pack();
                pack2.setYear(2025);
                pack2.setSemester(2);
                pack2.setName("Autumn 2025");
                packRepo.save(pack2);
            } else {
                List<Pack> packs = packService.getPacks();
                pack1 = packs.get(0);
                pack2 = packs.get(1);
            }

            for (int i = 0; i < 5; i++) {
                Course course = new Course();
                course.setType(random.nextBoolean() ? "Lecture" : "Lab");
                int randomIndex = 100 + random.nextInt(100);
                while (courseRepo.findByCode("CS" + randomIndex) != null) {
                    randomIndex = 100 + random.nextInt(100);
                }
                course.setCode("CS" + randomIndex);
                course.setAbbr("CS");
                course.setName(faker.book().title());
                course.setDescription(faker.lorem().sentence());
                course.setGroupCount(random.nextInt(10) + 1);
                course.setInstructor(random.nextBoolean() ? instructor1 : instructor2);
                course.setPack(random.nextBoolean() ? pack1 : pack2);
                System.out.println("CS" + randomIndex);
                courseRepo.save(course);
            }

            System.out.println("=== Database populated successfully ===");

            System.out.println("=== Testing CRUD operations ===");

            System.out.println("All courses:");
            courseRepo.findAll().forEach(c -> System.out.println(c.getId() + " - " + c.getName()));

            List<Course> courses = courseRepoImp.findByInstructor(instructor1);
            System.out.println("All courses by instructor 1:");
            courses.forEach(c -> System.out.println(c.getId() + " - " + c.getName()));

            courses = courseRepo.findByPackYearAndSemester(2025, 2);
            List<Course> courses2 = courseRepoImp.findByPackYearAndSemester(2025, 2);
            System.out.println("All courses by pack year and semester:");
            courses2.forEach(c -> System.out.println(c.getId() + " - " + c.getName()));
            courses.forEach(c -> System.out.println(c.getId() + " - " + c.getName()));


            Course firstCourse = courseRepo.findByInstructor(instructor1).getFirst();
            if (firstCourse != null) {
                System.out.println("Original instructor: " + firstCourse.getInstructor().getName());
//            firstCourse.setInstructor(instructor2);
                courseRepo.updateInstructorForCourse(firstCourse.getId(), instructor2.getId());
                System.out.println("Updated instructor: " + courseRepo.findById(firstCourse.getId()).get().getInstructor().getName());
            }
            Course lastCourse = courseRepo.findAll().get(courseRepo.findAll().size() - 1);
            System.out.println("Deleting course: " + lastCourse.getName());

            courseRepo.deleteByCode(lastCourse.getCode());
            System.out.println("Remaining courses count: " + courseRepo.count());
            courseRepo.deleteByCode("CS" + 1000);
            System.out.println("Remaining courses count: " + courseRepo.count());
            System.out.println("=== CRUD operations test complete ===");

        };
    }

    @Bean
    @Primary
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationManager authenticationManager, PackService packService)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/web/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus/**").permitAll()
                        .requestMatchers("/actuator/metrics").hasRole("ADMIN")
                        .requestMatchers("/api/packs").permitAll()
                        .requestMatchers("/api/instructor").hasRole("ADMIN")
                        .requestMatchers("/api/instructor").hasRole("INSTRUCTOR")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()// allow JSON login without CSRF token
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/web/login")        // POST → form submits here
                        .defaultSuccessUrl("/", true)  // after successful login
                        .permitAll()) // default login page
                .logout(logout -> logout.permitAll())
                .authenticationManager(authenticationManager)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        ; // /logout endpoint
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService(InstructorRepository instructorRepository, StudentRepository studentRepository) {
        return new DBUserDetailsService(instructorRepository, studentRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authBuilder.build();
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
//
//    @Bean("quickGrade")
//    public WebClient quickGradeWebClient(@Value("${quickgrade.base-url}") String baseUrl) {
//        return WebClient.builder()
//                .baseUrl(baseUrl)
//                .build();
//    }


    @Bean
    public WebSocketStompClient quickGradeStompClient() {
        var wsClient = new StandardWebSocketClient();
        var stompClient = new WebSocketStompClient(wsClient);
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
        return stompClient;
    }






    @Bean
    public ConsumerFactory<String, GradeEvent> gradeEventFinalConsumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        JacksonJsonDeserializer<GradeEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(GradeEvent.class);
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages(
                "com.example.lab4.dto",
                "com.example.lab4.service",
                "com.example.lab4.listeners",
                "com.example.lab7"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );
    }



    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GradeEvent>
    gradeEventFinalKafkaListenerContainerFactory(
            ConsumerFactory<String, GradeEvent> gradeEventFinalConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, GradeEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(gradeEventFinalConsumerFactory);
        return factory;
    }

}

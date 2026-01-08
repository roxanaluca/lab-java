package com.example.lab4.webfilter;

import com.example.lab4.repository.InstructorRepository;
import com.example.lab4.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class DBUserDetailsService implements UserDetailsService {

    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public DBUserDetailsService(InstructorRepository instructorRepository,
                                StudentRepository studentRepository) {
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {


        // 1. Admin
       return Optional.<UserDetails>ofNullable(null).or(() -> {
                    if ("admin".equals(username)) {
                        PasswordEncoder encoder = new BCryptPasswordEncoder();
                        return Optional.of(
                                User.withUsername("admin")
                                        .password(encoder.encode("qwerty1234"))
                                        .roles("ADMIN") // will become ROLE_ADMIN
                                        .build()
                        );
                    }
                    return Optional.empty();
                })

                .or(() -> Optional.ofNullable(instructorRepository.findByEmail(username)).map(instructor -> User.withUsername(instructor.getEmail())
                        .password(instructor.getPassword())
                        .roles("INSTRUCTOR")
                        .build()))

                // 3. Student
                .or(() -> Optional.ofNullable(studentRepository.findByEmail(username))
                        .map(student -> User.withUsername(student.getEmail())
                                .password(student.getPassword())
                                .roles("STUDENT")
                                .build()))

                .orElseThrow(() -> new UsernameNotFoundException(
                        "User email '%s' not found".formatted(username)));
    }
}

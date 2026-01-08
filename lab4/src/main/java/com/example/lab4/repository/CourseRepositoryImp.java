package com.example.lab4.repository;

import com.example.lab4.entity.Course;
import com.example.lab4.entity.Instructor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class CourseRepositoryImp {

    @PersistenceContext
    private EntityManager em;

    public List<Course> findByInstructor(Instructor instructor) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> course = cq.from(Course.class);
        if (instructor == null || instructor.getId() == null) {
            return Collections.emptyList();
        }

        cq.select(course)
                .where(cb.equal(course.get("instructor").get("id"), instructor.getId()));
        return em.createQuery(cq).getResultList();
    }

    public List<Course> findByPackYearAndSemester(int year, int semester) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Course> cq = cb.createQuery(Course.class);
        Root<Course> course = cq.from(Course.class);
        if (year <= 0 || semester <= 0) {
            return Collections.emptyList();
        }
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(course.get("pack").get("year"), year));
        predicates.add(cb.equal(course.get("pack").get("semester"), semester));
        cq.select(course)
                .where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    }
}

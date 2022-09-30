package com.edu.ulab.app.storage.jpa;

import com.edu.ulab.app.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<Person, Long> {
}

package com.edu.ulab.app.storage.memory;

import com.edu.ulab.app.entity.Person;

import java.util.Optional;

public interface UserInMemoryRepository {
    Person save(Person person);
    Person update(Person person);
    Person saveOrUpdate(Person person);
    Optional<Person> getUserById(Long id);
    void deleteUserById(Long id);
}

package com.edu.ulab.app.storage;

import com.edu.ulab.app.entity.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    User update(User user);
    User saveOrUpdate(User user);
    Optional<User> getUserById(Long id);
    void deleteUserById(Long id);
}

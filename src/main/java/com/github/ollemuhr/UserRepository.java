package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface UserRepository {
    User get(Integer id);
    Optional<User> getOpt(Integer id);
    User find(String username);
    Optional<User> findOpt(String username);
    User create(User user);
    Validation<List<Object>, User> createValid(Validation<List<Object>, User> user);
    Validation<String, User> update(User user);
}

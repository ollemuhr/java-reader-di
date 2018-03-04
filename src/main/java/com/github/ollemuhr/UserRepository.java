package com.github.ollemuhr;

import com.github.ollemuhr.user.User;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Optional;

/**
 *
 */
public interface UserRepository {
    Optional<User> get(Integer id);

    Optional<User> find(String username);

    Validation<Seq<String>, User> create(User user);

    Validation<Seq<String>, User> update(User user);
}

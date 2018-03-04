package com.github.ollemuhr;

import com.github.ollemuhr.user.User;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Optional;

/**
 */

public interface Users {
    default Configured<Config, Optional<User>> getUser(final Integer id) {
        return new Configured<>(config -> config.getUserRepository().get(id));
    }

    default Configured<Config, Optional<User>> findUser(final String username) {
        return new Configured<>(config -> config.getUserRepository().find(username));
    }

    default Configured<Config, Validation<Seq<String>, User>> create(final User user) {
        return new Configured<>(config -> config.getUserRepository().create(user));
    }

    default Configured<Config, Validation<Seq<String>, User>> update(final User user) {
        return new Configured<>(config -> config.getUserRepository().update(user));
    }
}

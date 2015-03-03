package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;
import java.util.Optional;

/**
 */

public interface Users {

    default public Reader<Config, User> getUser(final Integer id) {
        return new Reader<>(config -> config.getUserRepository().get(id));
    }

    default public Reader<Config, User> findUser(final String username) {
        return new Reader<>(config -> config.getUserRepository().find(username));
    }

    default public Reader<Config, Optional<User>> getUserOpt(final Integer id) {
        return new Reader<>(config -> config.getUserRepository().getOpt(id));
    }

    default public Reader<Config, Optional<User>> findUserOpt(final String username) {
        return new Reader<>(config -> config.getUserRepository().findOpt(username));
    }

    default public Reader<Config, User> create(final User user) {
        return new Reader<>(config -> config.getUserRepository().create(user));
    }

    default public Reader<Config, Validation<List<Object>, User>> createValid(final Validation<List<Object>, User> user) {
        return new Reader<>(config -> config.getUserRepository().createValid(user));
    }

    default public Reader<Config, Validation<String, User>> update(final User user) {
        return new Reader<>(config -> config.getUserRepository().update(user));
    }

}

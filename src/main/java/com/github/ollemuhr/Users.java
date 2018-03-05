package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;

/** Configured user repository methods. */
public interface Users {

  default Configured<Config, Option<User>> getUser(final Integer id) {
    return new Configured<>(config -> config.getUserRepository().get(id));
  }

  default Configured<Config, Option<User>> findUser(final String username) {
    return new Configured<>(config -> config.getUserRepository().find(username));
  }

  default Configured<Config, Validation<Seq<String>, User>> create(final User user) {
    return new Configured<>(config -> config.getUserRepository().create(user));
  }

  default Configured<Config, Validation<Seq<String>, User>> update(final User user) {
    return new Configured<>(config -> config.getUserRepository().update(user));
  }
}

package com.github.ollemuhr;

import io.trane.future.Future;
import java.util.List;
import java.util.Optional;

public final class Users {

  private final Config config;

  public Users(final Config config) {
    this.config = config;
  }

  Future<Optional<User>> getUser(final Long id) {
    return config.getUserRepository().get(id);
  }

  Future<Optional<User>> findUser(final String username) {
    return config.getUserRepository().find(username);
  }

  Future<List<User>> findAll() {
    return config.getUserRepository().findAll();
  }

  Future<User> create(final User user) {
    return config.getUserRepository().create(user);
  }

  Future<User> update(final User user) {
    return config.getUserRepository().update(user);
  }
}

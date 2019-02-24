package com.github.ollemuhr;

import io.trane.future.Future;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TestApp {
  private final UserManager userManager;
  private final Mailer mailer;
  private final Users users;

  TestApp(final Config config) {
    this.mailer = new Mailer(config);
    this.users = new Users(config);
    this.userManager = new UserManager(users);
  }

  Future<Optional<User>> findById(final Long id) {
    return users.getUser(id);
  }

  Future<Optional<User>> findByUsername(final String username) {
    return users.findUser(username);
  }

  Future<List<User>> findAll() {
    return users.findAll();
  }

  Future<Optional<String>> getUserMail(final Long id) {
    return users.getUser(id).map(option -> option.map(User::getEmail));
  }

  Future<Optional<Map<String, String>>> getUserInfo(final String username) {
    return userManager.userInfo(username);
  }

  Future<Optional<User>> findBossOf(final String username) {
    return userManager.findBossOf(username);
  }

  Future<User> create(final User user) {
    return users.create(user);
  }

  Future<User> update(final User user) {
    return users.update(user);
  }

  Future<User> createValidAndMail(final User user) {
    return users.create(user).onSuccess(mailer::send);
  }
}

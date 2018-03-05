package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import java.util.Map;

/** Showing how to apply config. */
public class TestApp {

  private final Config config;

  private final UserManager userManager = new UserManager();

  public TestApp(final Config config) {
    this.config = config;
  }

  public Option<User> findById(final Integer id) {
    return userManager.getUser(id).apply(config);
  }

  public Option<User> findByUsername(final String username) {
    return userManager.findUser(username).apply(config);
  }

  Option<String> getUserMail(final Integer id) {
    return userManager.getUser(id).map(user -> user.map(User::getEmail)).apply(config);
  }

  public Option<Map<String, String>> getUserInfo(final String username) {
    return userManager.userInfo(username).apply(config);
  }

  public Option<User> findBossOf(final String username) {
    return userManager.findBossOf(username).apply(config);
  }

  public Validation<Seq<String>, User> create(final User user) {
    return userManager.create(user).apply(config);
  }

  public Validation<Seq<String>, User> update(final User user) {
    return userManager.update(user).apply(config);
  }

  public Validation<Seq<String>, User> createValidAndMail(final User user) {
    return userManager.createValidAndMail(user).apply(config);
  }
}

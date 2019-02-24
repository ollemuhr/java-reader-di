package com.github.ollemuhr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.trane.future.CheckedFutureException;
import io.trane.future.Future;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Testing it. */
@SuppressWarnings("ConstantConditions")
abstract class UserTest {

  static final User u1 = User.valid(1L, null, "Mrone", "Oner", "Mrone@Oner.se", "mrone");
  static final User u2 = User.valid(2L, 1L, "Mrtwo", "Twoer", "Mrtwo@Twoer.se", "mrtwo");

  private TestApp app;

  private Duration timeout = Duration.ofSeconds(1);

  UserTest() {
    app = app();
  }

  @Test
  void testUserInfo() throws CheckedFutureException {
    assertEquals("Mrone Oner", app.getUserInfo(u2.getUsername()).get(timeout).get().get("boss"));
  }

  @Test
  void testBoss() throws CheckedFutureException {
    assertEquals("Mrone", app.findBossOf(u2.getUsername()).get(timeout).get().getFirstName());
  }

  @Test
  void testUserEmail() throws CheckedFutureException {
    assertEquals("Mrone@Oner.se", app.getUserMail(u1.getId()).get(timeout).get());
  }

  @Test
  void testById() throws CheckedFutureException {
    assertEquals(u1.getUsername(), app.findById(u1.getId()).get(timeout).get().getUsername());
  }

  @Test
  void testByUsername() throws CheckedFutureException {
    assertEquals(
        u2.getUsername(), app.findByUsername(u2.getUsername()).get(timeout).get().getUsername());
  }

  @Test
  void testCreate() throws CheckedFutureException {
    final User toCreate = User.valid(null, 2L, "Mrthree", "Threer", "Mrthree@Threer", "mrthree");
    final Future<User> stored = app.create(toCreate);

    assertEquals("Mrthree@Threer", stored.get(timeout).getEmail());
  }

  @Test
  void testCreateBoom() throws CheckedFutureException {
    final User valid = User.valid(null, u1.getId(), "Boomer", "Boomer", "b@b.se", "boomer");

    final Future<User> boom = app.create(valid);

    final String result =
        boom.map(__ -> "We should not go here")
            .rescue(e -> Future.value(e.getMessage()))
            .get(timeout);

    assertTrue(result.contains("boom"));
  }

  @Test
  void testUpdate() throws CheckedFutureException {
    final User newUsername =
        User.valid(
            u1.getId(),
            u1.getSupervisorId(),
            u1.getFirstName(),
            u1.getLastName(),
            u1.getEmail(),
            "newUsername");

    app.update(newUsername);

    assertEquals("newUsername", app.findById(u1.getId()).get(timeout).get().getUsername());
  }

  @Test
  void testUpdateNonExist() throws CheckedFutureException {
    final User newUsername =
        User.valid(
            -1L,
            u1.getSupervisorId(),
            u1.getFirstName(),
            u1.getLastName(),
            u1.getEmail(),
            "newUsername");

    final Future<User> updated = app.update(newUsername);

    updated
        .onSuccess(__ -> fail())
        .rescue(
            e -> {
              if (e instanceof ValidationError) {
                var ve = (ValidationError) e;
                assertEquals(1, ve.errors().size());
                assertEquals("user.not.exists", ve.errors().get(0));
              } else {
                fail();
              }
              return Future.value(null);
            })
        .get(timeout);
  }

  @Test
  void createAndMail() throws CheckedFutureException {
    final Future<User> userFuture =
        app.createValidAndMail(User.valid(null, 1L, "Youve", "Gotmail", "a@b.se", "youvegotmail"));

    final User valid = userFuture.get(timeout);

    assertTrue(valid.getId() > 0);
  }

  @Test
  void testCreateBadName() {
    try {
      User.valid(null, 1L, "You've", "Gotm'ail", "a@b.se", "mrone");
      fail();
    } catch (ValidationError e) {
      assertEquals(2, e.errors().size());
      assertTrue(e.errors().contains("user.firstName.invalid"));
      assertTrue(e.errors().contains("user.lastName.invalid"));
    }
  }

  @Test
  void testCreateSameUser() throws CheckedFutureException {
    try {
      app.create(u1).get(timeout);
      fail();
    } catch (ValidationError e) {
      assertEquals(1, e.errors().size());
      assertEquals("user.unique.constraint", e.errors().get(0));
    }
  }

  private static User user(final int i) {
    return User.valid(null, 1L, "first", "last", i + "a@b.se", "username" + i);
  }

  abstract Config getConfig();

  private TestApp app() {
    return new TestApp(getConfig());
  }
}

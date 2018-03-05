package com.github.ollemuhr;

import static com.github.ollemuhr.UserTest.TestConf.u1;
import static com.github.ollemuhr.UserTest.TestConf.u2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.vavr.CheckedFunction0;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

/** Testing it. */
@SuppressWarnings("ConstantConditions")
public class UserTest {

  private TestApp app;

  @Before
  public void before() {
    this.app = app();
  }

  @Test
  public void testUserInfo() {
    assertEquals("Mrone Oner", app.getUserInfo(u2.getUsername()).get().get("boss"));
  }

  @Test
  public void testBoss() {
    assertEquals("Mrone", app.findBossOf(u2.getUsername()).get().getFirstName());
  }

  @Test
  public void testUserEmail() {
    assertEquals("Mrone@Oner.se", app.getUserMail(u1.getId()).getOrElse("not found"));
  }

  @Test
  public void testById() {
    assertEquals(u1.getUsername(), app.findById(u1.getId()).get().getUsername());
  }

  @Test
  public void testByUsername() {
    assertEquals(u2.getUsername(), app.findByUsername(u2.getUsername()).get().getUsername());
  }

  @Test
  public void testCreate() {
    final Validation<Seq<String>, User> stored =
        User.valid(null, 2, "Mrthree", "Threer", "Mrthree@Threer", "mrthree")
            .flatMap(user -> app.create(user));
    assertEquals("Mrthree@Threer", stored.get().getEmail());
  }

  @Test
  public void testCreateBoom() {
    final Validation<Seq<String>, User> valid =
        User.valid(null, u1.getId(), "Boomer", "Boomer", "b@b.se", "boomer");

    final Try<Validation<Seq<String>, User>> boom =
        Try.of(() -> valid.flatMap(user -> app.create(user)));

    final String result = boom.map(assertNoValidation()).recover(Throwable::getMessage).get();

    assertEquals("boom", result);
  }

  private static <E, T> Function<Validation<E, T>, String> assertNoValidation() {
    return user ->
        user.fold(
            e -> {
              fail();
              return null;
            },
            u -> {
              fail();
              return (String) null;
            });
  }

  @Test
  public void testUpdate() {
    final Validation<Seq<String>, User> newUsername =
        User.valid(
            u1.getId(),
            u1.getSupervisorId(),
            u1.getFirstName(),
            u1.getLastName(),
            u1.getEmail(),
            "newUsername");

    final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app.update(user));

    assertTrue(updated.isValid());
    assertEquals("newUsername", app.findById(u1.getId()).get().getUsername());
  }

  @Test
  public void testUpdateNonExist() {
    final Validation<Seq<String>, User> newUsername =
        User.valid(
            -1,
            u1.getSupervisorId(),
            u1.getFirstName(),
            u1.getLastName(),
            u1.getEmail(),
            "newUsername");

    assertTrue(newUsername.isValid());

    final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app.update(user));

    assertFalse(updated.isValid());
    updated.fold(
        e -> {
          assertEquals(1, e.size());
          assertEquals("user.not.exists", e.head());
          return null;
        },
        user -> {
          fail();
          return null;
        });
  }

  @Test
  public void createAndMail() {
    final Validation<Seq<String>, User> valid =
        app.createValidAndMail(
            User.valid(null, 1, "Youve", "Gotmail", "a@b.se", "youvegotmail").get());

    assertTrue(valid.isValid());

    valid.fold(
        e -> {
          fail();
          return null;
        },
        user -> {
          assertTrue(user.getId() > 0);
          return null;
        });
  }

  @Test
  public void testCreateBadName() {
    final Validation<Seq<String>, User> invalid =
        User.valid(null, 1, "You've", "Gotm'ail", "a@b.se", "mrone");

    assertFalse(invalid.isValid());

    invalid.fold(
        e -> {
          assertEquals(2, e.size());
          assertTrue(e.contains("user.firstName.invalid"));
          assertTrue(e.contains("user.lastName.invalid"));
          return null;
        },
        user -> {
          fail();
          return null;
        });
  }

  @Test
  public void testCreateSameUser() {
    final Validation<Seq<String>, User> invalid = app.create(u1);

    assertFalse(invalid.isValid());

    invalid.fold(
        e -> {
          assertEquals(1, e.size());
          assertEquals("user.unique.constraint", e.head());
          return null;
        },
        user -> {
          fail();
          return null;
        });
  }

  private TestApp app() {
    return new TestApp(new TestConf().config());
  }

  /** Fake implementations of UserRepository and MailService. */
  static class TestConf {

    static final AtomicInteger idGen = new AtomicInteger(0);
    static final User u1 =
        User.valid(idGen.incrementAndGet(), -1, "Mrone", "Oner", "Mrone@Oner.se", "mrone").get();
    static final User u2 =
        User.valid(idGen.incrementAndGet(), u1.getId(), "Mrtwo", "Twoer", "Mrtwo@Twoer.se", "mrtwo")
            .get();

    private final Map<Integer, User> byId = init();

    private Map<Integer, User> init() {
      final Map<Integer, User> m = new HashMap<>();
      m.put(u1.getId(), u1);
      m.put(u2.getId(), u2);
      return m;
    }

    Config config() {
      return new Config() {
        @Override
        public UserRepository getUserRepository() {
          return new UserRepository() {
            @Override
            public Option<User> get(final Integer id) {
              return Option.of(byId.get(id));
            }

            @Override
            public Option<User> find(final String username) {
              return Option.ofOptional(
                  byId.values().stream().filter(u -> u.getUsername().equals(username)).findFirst());
            }

            @Override
            public Validation<Seq<String>, User> create(final User user) {
              return Try.of(() -> store(user))
                  .map(Validation::<Seq<String>, User>valid)
                  .recover(
                      UserExistsException.class, e -> Validation.invalid(Vector.of(e.getMessage())))
                  .get();
            }

            @Override
            public Validation<Seq<String>, User> update(final User user) {
              return Try.of(tryUpdate(user))
                  .recover(
                      UserExistsException.class, e -> Validation.invalid(List.of(e.getMessage())))
                  .get();
            }

            private CheckedFunction0<Validation<Seq<String>, User>> tryUpdate(final User user) {
              return () ->
                  get(user.getId())
                      .map(putUser(user))
                      .map(Validation::<Seq<String>, User>valid)
                      .getOrElseThrow(() -> new UserExistsException("user.not.exists"));
            }

            private Function<User, User> putUser(final User user) {
              return __ -> {
                byId.put(user.getId(), user);
                return user;
              };
            }

            private User store(final User user) throws SQLException {
              if (find(user.getUsername()).isDefined()) {
                throw new UserExistsException("user.unique.constraint");
              }
              if ("boomer".equals(user.getUsername())) {
                throw new SQLException("boom");
              }
              final User toInsert = user.withId(idGen.incrementAndGet());
              byId.put(toInsert.getId(), toInsert);
              return toInsert;
            }
          };
        }

        void print(String s, String... arg) {
          System.out.println(String.format(s, arg));
        }

        void print(String s) {
          System.out.println(s);
        }

        @Override
        public MailService getMailService() {
          return m -> {
            print("pretending to send a toMail:");
            print("from:\t\t%s", m.getFrom());
            print("to:\t\t\t%s", m.getTo());
            print("subject:\t%s", m.getSubject());
            print("message:\t%s", m.getMessage());

            return null;
          };
        }
      };
    }
  }
}

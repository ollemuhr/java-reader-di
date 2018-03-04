package com.github.ollemuhr;

import com.github.ollemuhr.user.User;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;

/**
 *
 */
class TestConf {

    final AtomicInteger idGen = new AtomicInteger(0);
    private final Map<Integer, User> fromId = init();

    private Map<Integer, User> init() {
        final Map<Integer, User> m = new HashMap<>();
        final User u1 = User.valid(idGen.incrementAndGet(), -1, "Mrone", "Oner", "Mrone@Oner.se", "mrone").get();
        final User u2 = User.valid(idGen.incrementAndGet(), u1.getId(), "Mrtwo", "Twoer", "Mrtwo@Twoer.se", "mrtwo").get();
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
                    public Optional<User> get(final Integer id) {
                        return Optional.ofNullable(fromId.get(id));
                    }

                    @Override
                    public Optional<User> find(final String username) {
                        try {
                            return Optional.ofNullable(fromId.entrySet().stream()
                                    .collect(toMap(e -> e.getValue().getUsername(), Map.Entry::getValue))
                                    .get(username));
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }

                    User store(final User user) {

                        find(user.getUsername()).ifPresent(u -> {
                            throw new RuntimeException("user.unique.constraint");
                        });
                        final User toInsert = user.withId(idGen.incrementAndGet());
                        fromId.put(toInsert.getId(), toInsert);
                        return toInsert;
                    }

                    @Override
                    public Validation<Seq<String>, User> create(final User user) {
                        try {
                            return Validation.valid(store(user));
                        } catch (Exception e) {
                            return Validation.invalid(Vector.of(e.getMessage()));
                        }
                    }

                    @Override
                    public Validation<Seq<String>, User> update(final User user) {

                        try {
                            get(user.getId()).map(u ->
                                    fromId.put(user.getId(), user))
                                    .orElseThrow(() ->
                                            new RuntimeException("user.not.exists"));

                            return Validation.valid(user);

                        } catch (Exception e) {
                            return Validation.invalid(List.of(e.getMessage()));
                        }
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
                    print("pretending to send a mail:");
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

public class UserTest extends TestConf {
    @Test
    public void testUserInfo() {
        assertEquals("Mrone Oner", app().getUserInfo("mrtwo").get().get("boss"));
    }

    @Test
    public void testBoss() {
        assertEquals("Mrone", app().findBossOf("mrtwo").get().getFirstName());
    }

    @Test
    public void testUserEmail() {
        assertEquals("Mrone@Oner.se", app().getUserMail(1).orElse("not found"));
    }

    @Test
    public void testById() {
        assertEquals("mrone", app().findById(1).get().getUsername());
    }

    @Test
    public void testByUsername() {
        assertEquals("mrtwo", app().findByUsername("mrtwo").get().getUsername());
    }

    @Test
    public void testCreate() {
        final Validation<Seq<String>, User> stored = User.valid(null, 2, "Mrthree", "Threer", "Mrthree@Threer", "mrthree")
                .flatMap(user -> app().create(user));
        assertEquals("Mrthree@Threer", stored.get().getEmail());
    }

    @Test
    public void testUpdate() {
        final User u = app().findById(1).get();
        final Validation<Seq<String>, User> newUsername =
                User.valid(u.getId(), u.getSupervisorId(), u.getFirstName(), u.getLastName(), u.getEmail(), "newUsername");

        final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app().update(user));

        assertTrue(updated.isValid());
        assertEquals("newUsername", app().findById(1).get().getUsername());
    }

    @Test
    public void testUpdateNonExist() {
        final User u = app().findById(1).get();
        final Validation<Seq<String>, User> newUsername =
                User.valid(-1, u.getSupervisorId(), u.getFirstName(), u.getLastName(), u.getEmail(), "newUsername");

        assertTrue(newUsername.isValid());

        final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app().update(user));

        assertFalse(updated.isValid());
        assertEquals("user.not.exists", updated.getError().head());
        assertEquals(u.getUsername(), app().findById(1).get().getUsername());
    }

    @Test
    public void mailToConsoleOpt() {
        final Validation<Seq<String>, User> v = app().createValidAndMail(User.valid(null, 1, "Youve", "Gotmail", "a@b.se", "youvegotmail").get());
        assertTrue(v.isValid());
        assertTrue(v.get().getId() > 0);
    }

    @Test
    public void testCreateBadName() {
        final Validation<Seq<String>, User> invalid = User.valid(null, 1, "You've", "Gotm'ail", "a@b.se", "mrone");
        System.out.println(invalid.getError());
        assertFalse(invalid.isValid());
    }

    @Test
    public void testCreateSameUser() {
        final User found = app().findById(1).get();
        final Validation<Seq<String>, User> invalid = app().create(found);
        assertEquals("user.unique.constraint", invalid.getError().head());
        assertFalse(invalid.isValid());
    }

    private TestAppl app() {
        return new TestAppl(config());
    }
}

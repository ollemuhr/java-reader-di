package com.github.ollemuhr;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 *
 */
class TestConf {
    final AtomicInteger idGen = new AtomicInteger(0);
    final User u1 = User.valid(idGen.incrementAndGet(), -1, "Mrone", "Oner", "Mrone@Oner.se", "mrone").get();
    final User u2 = User.valid(idGen.incrementAndGet(), u1.getId(), "Mrtwo", "Twoer", "Mrtwo@Twoer.se", "mrtwo").get();
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
                    public Optional<User> get(final Integer id) {
                        return Optional.ofNullable(byId.get(id));
                    }

                    @Override
                    public Optional<User> find(final String username) {
                        return byId.values().stream()
                                .filter(u -> u.getUsername().equals(username))
                                .findFirst();
                    }

                    @Override
                    public Validation<Seq<String>, User> create(final User user) {
                        return Try.<Validation<Seq<String>, User>>of(() -> Validation.valid(store(user)))
                                .recover(e -> Validation.invalid(Vector.of(e.getMessage())))
                                .get();
                    }

                    @Override
                    public Validation<Seq<String>, User> update(final User user) {
                        return Try.<Validation<Seq<String>, User>>of(() -> Validation.valid(get(user.getId())
                                .map(found -> {
                                    byId.put(user.getId(), user);
                                    return user;
                                }).orElseThrow(() -> new RuntimeException("user.not.exists"))))
                                .recover(e -> Validation.invalid(List.of(e.getMessage()))).get();
                    }

                    private User store(final User user) {
                        find(user.getUsername()).ifPresent(u -> {
                            throw new RuntimeException("user.unique.constraint");
                        });
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
        assertEquals("Mrone@Oner.se", app().getUserMail(u1.getId()).orElse("not found"));
    }

    @Test
    public void testById() {
        assertEquals("mrone", app().findById(u1.getId()).get().getUsername());
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
        final User u = app().findById(u1.getId()).get();
        final Validation<Seq<String>, User> newUsername =
                User.valid(u.getId(), u.getSupervisorId(), u.getFirstName(), u.getLastName(), u.getEmail(), "newUsername");

        final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app().update(user));

        assertTrue(updated.isValid());
        assertEquals("newUsername", app().findById(u1.getId()).get().getUsername());
    }

    @Test
    public void testUpdateNonExist() {
        final User u = app().findById(1).get();
        final Validation<Seq<String>, User> newUsername =
                User.valid(-1, u.getSupervisorId(), u.getFirstName(), u.getLastName(), u.getEmail(), "newUsername");

        assertTrue(newUsername.isValid());

        final Validation<Seq<String>, User> updated = newUsername.flatMap(user -> app().update(user));

        assertFalse(updated.isValid());
        updated.fold(e -> {
                    assertEquals(1, e.size());
                    assertEquals("user.not.exists", e.head());
                    return null;
                }, user -> {
                    fail();
                    return null;
                }
        );
        assertEquals(u.getUsername(), app().findById(1).get().getUsername());
    }

    @Test
    public void createAndMail() {
        final Validation<Seq<String>, User> valid = app().createValidAndMail(User.valid(null, 1, "Youve", "Gotmail", "a@b.se", "youvegotmail").get());
        assertTrue(valid.isValid());
        valid.fold(e -> {
                    fail();
                    return null;
                }, user -> {
                    assertTrue(user.getId() > 0);
                    return null;
                }
        );
    }

    @Test
    public void testCreateBadName() {
        final Validation<Seq<String>, User> invalid = User.valid(null, 1, "You've", "Gotm'ail", "a@b.se", "mrone");
        assertFalse(invalid.isValid());
        invalid.fold(e -> {
            assertEquals(2, e.size());
            assertTrue(e.contains("user.firstname.invalid"));
            assertTrue(e.contains("user.lastname.invalid"));
            return null;
        }, user -> {
            fail();
            return null;
        });
    }

    @Test
    public void testCreateSameUser() {
        final User found = app().findById(1).get();
        final Validation<Seq<String>, User> invalid = app().create(found);

        assertFalse(invalid.isValid());

        invalid.fold(e -> {
            assertEquals(1, e.size());
            assertEquals("user.unique.constraint", e.head());
            return null;
        }, user -> {
            fail();
            return null;
        });
    }

    private TestApp app() {
        return new TestApp(config());
    }
}

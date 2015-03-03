package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.ollemuhr.validation.Validation.failure;
import static java.util.Optional.ofNullable;
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
        final User u1 = new User(idGen.incrementAndGet(), -1, "Mrone", "Oner", "Mrone@Oner.se", "mrone");
        final User u2 = new User(idGen.incrementAndGet(), u1.getId(), "Mrtwo", "Twoer", "Mrtwo@Twoer.se", "mrtwo");
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
                    public User get(final Integer id) {
                        return fromId.get(id);
                    }

                    @Override
                    public Optional<User> getOpt(final Integer id) {
                        return ofNullable(get(id));
                    }

                    @Override
                    public User find(final String username) {

                        return fromId.entrySet().stream()
                                .collect(toMap(e -> e.getValue().getUsername(), Map.Entry::getValue))
                                .get(username);
                    }

                    @Override
                    public Optional<User> findOpt(final String username) {
                        return ofNullable(find(username));
                    }

                    @Override
                    public User create(final User user) {
                        findOpt(user.getUsername()).ifPresent(u -> {
                            throw new RuntimeException("user.unique.constraint");
                        });
                        final User toInsert = user.withId(idGen.incrementAndGet());
                        fromId.put(toInsert.getId(), toInsert);
                        return toInsert;
                    }

                    @Override
                    public Validation<List<Object>, User> createValid(Validation<List<Object>, User> user) {
                        try {
                            return user.map(this::create);
                        } catch (Exception e) {
                            return user.flatMap(u -> failure(e.getMessage(), u));
                        }
                    }

                    @Override
                    public Validation<String, User> update(final User user) {
                        try {
                            getOpt(user.getId()).map(u ->
                                    fromId.put(user.getId(), user))
                                    .orElseThrow(() ->
                                            new RuntimeException("user.not.exists"));

                            return Validation.success(user);

                        } catch (Exception e) {

                            return Validation.failure(e.getMessage(), user);
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
                return new MailService() {
                    @Override
                    public Validation<List<Object>, Void> send(Mail mail) {
                        return null;
                    }

                    @Override
                    public Validation<List<Object>, Void> sendValid(Validation<List<Object>, Mail> mail) {
                        return mail.map(m -> {
                                    print("pretending to send a mail:");
                                    print("from:\t\t%s", m.getFrom());
                                    print("to:\t\t\t%s", m.getTo());
                                    print("subject:\t%s", m.getSubject());
                                    print("message:\t%s", m.getMessage());
                                    return null;
                                }
                        );
                    }
                };
            }
        };
    }

}

public class UserTest extends TestConf {

    @Test
    public void testUserInfo() {

        assertEquals("Mrone Oner", app().getUserInfo("mrtwo").get("boss"));
    }

    @Test
    public void testUserEmail() {

        assertEquals("Mrone@Oner.se", app().getUserMail(1).orElse("not found"));
    }

    @Test
    public void testById() {
        assertEquals("mrone", app().findById(1).getUsername());
    }

    @Test
    public void testByUsername() {
        assertEquals("mrtwo", app().findByUsername("mrtwo").getUsername());
    }

    @Test
    public void testIdOpt() {
        final String username = app().findByIdOpt(1).map(User::getUsername).orElse("not found");
        assertEquals("mrone", username);
    }

    @Test
    public void testUsernameOpt() {
        final String username = app().findByUserNameOpt("mrtwo").map(User::getUsername).orElse("not found");
        assertEquals("mrtwo", username);
    }

    @Test
    public void testUserInfoOpt() {
        final String boss = app().getUserInfoOpt("mrtwo").map(m -> m.get("boss")).orElse("not found");
        assertEquals("Mrone Oner", boss);
    }

    @Test
    public void testCreate() {
        final User u = app().create(new User(null, 2, "Mrthree", "Threer", "Mrthree@Threer", "mrthree"));
        assertEquals("Mrthree@Threer", u.getEmail());
    }

    @Test
    public void testUpdate() {
        final TestAppl app = app();
        final User u = app.findById(1);
        final User toUpdate = new User(u.getId(), u.getSupervisorId(), u.getFirstName(), u.getLastName(), u.getEmail(), "newUsername");
        assertTrue(app.update(toUpdate).isSuccess());
        assertEquals("newUsername", app.findById(1).getUsername());
    }


    @Test
    public void mailToConsoleOpt() {
        final Validation<List<Object>, User> v = app().createValidAndMail(new User(null, 1, "Youve", "Gotmail", "a@b.se", "youvegotmail"));
        assertTrue(v.isSuccess());
        assertTrue(v.value().getId() > 0);
    }

    @Test
    public void testCreateThrows() {
        final Validation<List<Object>, User> v = app().createValidAndMail(new User(null, 1, "You've", "Gotm'ail", "a@b.se", "mrone"));
        System.out.println(v.failure());
        assertFalse(v.isSuccess());
    }

    private TestAppl app() {
        return new TestAppl(config());
    }
}

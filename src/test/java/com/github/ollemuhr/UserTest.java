package com.github.ollemuhr;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;

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
                        return ofNullable(fromId.get(id));
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
                        getOpt(user.getId()).ifPresent(u -> {
                            throw new IllegalArgumentException("Already exist");
                        });
                        final User toInsert = user.withId(idGen.incrementAndGet());
                        fromId.put(toInsert.getId(), toInsert);
                        return toInsert;
                    }

                    @Override
                    public Void update(final User user) {
                        getOpt(user.getId()).map(u ->
                                fromId.put(user.getId(), user))
                                .orElseThrow(() ->
                                        new IllegalArgumentException("Not found"));
                        return null;
                    }
                };
            }

            @Override
            public MailService getMailService() {
                return new MailService() {
                    @Override
                    public Void send(String from, String to, String subject, String message) {
                        out.println("pretending to send a mail:");
                        out.println(String.format("from:\t\t%s", from));
                        out.println(String.format("to:\t\t\t%s", to));
                        out.println(String.format("subject:\t%s", subject));
                        out.println(String.format("message:\t%s", message));
                        return null;
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

        assertEquals("Mrone@Oner.se", app().getUserMail(1));
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
        final User toUpdate = new User(u.getId(),u.getSupervisorId(),u.getFirstName(),u.getLastName(),u.getEmail(),"newUsername");
        app.update(toUpdate);
        assertEquals("newUsername", app.findById(1).getUsername());
    }

    @Test
    public void mailToConsole() {
        app().createAndMail(new User(null, 1, "You've", "Gotmail", "a@b.se", "youvegotmail"));
    }
    private TestAppl app() {
        return new TestAppl(config());
    }
}

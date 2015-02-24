package com.github.ollemuhr;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 */
public class UserManager implements Users, Mailer {

    public Reader<Config, String> userEmail(final Integer id) {
        return getUser(id).map(User::getEmail);
    }

    private BiFunction<User, User, Map<String, String>> toMap = (user, boss) -> {
        final Map<String, String> m = new HashMap<>();
        m.put("fullname", user.getFirstName() + " " + user.getLastName());
        m.put("email", user.getEmail());
        m.put("boss", boss.getFirstName() + " " + boss.getLastName());
        return m;
    };

    public Reader<Config, Map<String, String>> userInfo(final String username) {
        return findUser(username).flatMap(u ->
                        getUser(u.getSupervisorId()).map(boss ->
                                toMap.apply(u, boss))
        );
    }

    /**
     * Little more complex
     * and it is getting messy.
     *
     * @param username
     * @return
     */
    public Reader<Config, Optional<Map<String, String>>> userInfoOpt(final String username) {
        return findUserOpt(username).flatMap(userOpt ->
                userOpt.map(user ->
                        getUserOpt(user.getSupervisorId()).map(bossOpt -> bossOpt.map(boss ->
                                toMap.apply(user, boss))))
                        .orElse(new Reader<>(c -> Optional.empty())));
    }

    public Reader<Config, User> findBossOf(final String username) {
        return findUser(username).flatMap(u ->
                getUser(u.getSupervisorId()));
    }

    public Reader<Config, Void> createAndMail(final User user) {
        return create(user).flatMap(u ->
                send("the mailer", u.getEmail(), "your account", "your username: " + u.getUsername()));
    }
}

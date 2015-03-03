package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public class UserManager implements Users, Mailer {

    public Reader<Config, Optional<String>> userEmail(final Integer id) {
        return getUserOpt(id).map(opt -> opt.map(User::getEmail));
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
                        getUserOpt(user.getSupervisorId()).map(bossOpt ->
                                bossOpt.map(boss ->
                                        toMap.apply(user, boss))))
                        .orElse(new Reader<>(c -> Optional.empty())));
    }

    public Reader<Config, User> findBossOf(final String username) {
        return findUser(username).flatMap(u ->
                getUser(u.getSupervisorId()));
    }

    public Reader<Config, Validation<List<Object>, User>> createValidAndMail(final User user) {

        final Function<User, Mail> mail = u -> new Mail("the mailer", u.getEmail(), "your account", "your id: " + u.getId());

        return createValid(User.validate(user))
                .flatMap(v ->
                        sendValid(v.map(mail)).map(x -> v));

    }

}

package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 */
public class UserManager implements Users, Mailer {
    private BiFunction<User, User, Map<String, String>> toMap = (user, boss) -> {
        final Map<String, String> m = new HashMap<>();
        m.put("fullname", user.getFirstName() + " " + user.getLastName());
        m.put("email", user.getEmail());
        m.put("boss", boss.getFirstName() + " " + boss.getLastName());
        return m;
    };

    public Configured<Config, Optional<Map<String, String>>> userInfo(final String username) {
        return new Configured<>(config ->
                findUser(username).apply(config)
                        .flatMap(u -> getUser(u.getSupervisorId()).apply(config)
                                .map(boss -> toMap.apply(u, boss))));
    }

    public Configured<Config, Optional<User>> findBossOf(final String username) {
        return new Configured<>(config ->
                findUser(username).apply(config)
                        .flatMap(u -> getUser(u.getSupervisorId()).apply(config)));
    }

    public Configured<Config, Validation<Seq<String>, User>> createValidAndMail(final User user) {
        return new Configured<>(config -> {
            final Validation<Seq<String>, User> valid = create(user).apply(config);
            Try.of(() -> valid.flatMap(u -> send(u).apply(config)))
                    .onFailure(t -> System.out.println(t.getMessage()));
            return valid;
        });
    }
}

package com.github.ollemuhr;

import com.github.ollemuhr.user.User;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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
                        .flatMap(u ->
                                getUser(u.getSupervisorId()).apply(config)));
    }

    public C<User> createValidAndMail(final User user) {
        return new C<>(config -> {
            final Validation<Seq<String>, User> valid = create(user).apply(config);
            Try.of(() -> valid.flatMap(u -> send(u.mail()).apply(config)))
                    .onFailure(t -> System.out.println(t.getMessage()));
            return valid;
        });
    }

    public class C<T> extends Configured<Config, Validation<Seq<String>, T>> {
        public C(Function<Config, Validation<Seq<String>, T>> run) {
            super(run);
        }
    }
}

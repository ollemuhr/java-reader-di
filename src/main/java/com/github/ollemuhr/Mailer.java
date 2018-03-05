package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Objects;

/**
 *
 */
public interface Mailer {
    default Mail mail(final User user) {
        Objects.requireNonNull(user.getId());
        Objects.requireNonNull(user.getEmail());
        return new Mail("the mailer",
                user.getEmail(),
                "your account",
                "your id: " + user.getId());
    }

    default Configured<Config, Validation<Seq<String>, Void>> send(final User user) {
        return new Configured<>(config -> config.getMailService().send(mail(user)));
    }
}

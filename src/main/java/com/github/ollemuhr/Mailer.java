package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

/**
 *
 */
public interface Mailer {
    default Configured<Config, Validation<Seq<String>, Void>> send(final Mail mail) {
        return new Configured<>(config ->
                config.getMailService().send(mail));
    }
}

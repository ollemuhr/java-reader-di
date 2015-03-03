package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;

/**
 *
 */
public interface Mailer {

    default public Reader<Config, Validation<List<Object>, Void>> send(final Mail mail) {
        return new Reader<>(c -> c.getMailService().send(mail));
    }

    default public Reader<Config, Validation<List<Object>, Void>> sendValid(final Validation<List<Object>, Mail> mail) {
        return new Reader<>(c -> c.getMailService().sendValid(mail));
    }

}

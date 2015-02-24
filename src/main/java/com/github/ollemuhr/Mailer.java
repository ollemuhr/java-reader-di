package com.github.ollemuhr;

/**
 *
 */
public interface Mailer {

    default public Reader<Config, Void> send(
            final String from,
            final String to,
            final String subject,
            final String message) {
        return new Reader<>(c -> c.getMailService().send(from,to,subject,message));
    }
}

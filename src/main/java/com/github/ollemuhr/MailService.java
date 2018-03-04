package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;

/**
 *
 */
public interface MailService {
    Validation<Seq<String>, Void> send(Mail mail);
}

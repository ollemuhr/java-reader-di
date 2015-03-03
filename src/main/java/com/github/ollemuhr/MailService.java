package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;

/**
 *
 */
public interface MailService {
    Validation<List<Object>, Void> send(Mail mail);

    Validation<List<Object>, Void> sendValid(Validation<List<Object>, Mail> mail);
}

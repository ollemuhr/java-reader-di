package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import java.util.Objects;

/** Configured mail sender. */
public interface Mailer {

  default Configured<Config, Validation<Seq<String>, Void>> send(final User user) {
    return new Configured<>(config -> config.getMailService().send(toMail(user)));
  }

  /**
   * User to mail.
   *
   * @param user the user.
   * @return the mail.
   */
  default MailService.Mail toMail(final User user) {
    Objects.requireNonNull(user.getId());
    Objects.requireNonNull(user.getEmail());
    return new MailService.Mail(
        "the mailer", user.getEmail(), "your account", "your id: " + user.getId());
  }
}

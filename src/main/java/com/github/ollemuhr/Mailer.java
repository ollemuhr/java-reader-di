package com.github.ollemuhr;

import io.trane.future.Future;
import java.util.Objects;

/** Configured mail sender. */
final class Mailer {

  private final Config config;

  Mailer(final Config config) {
    this.config = config;
  }

  Future<Void> send(final User user) {
    final var emailFuture = Future.apply(() -> toMail(user));
    return emailFuture
        .flatMap(email -> config.getMailService().send(email))
        .onFailure(Throwable::printStackTrace)
        .rescue(e -> Future.VOID);
  }

  /**
   * User to mail.
   *
   * @param user the user.
   * @return the mail.
   */
  private static MailService.Mail toMail(final User user) {
    Objects.requireNonNull(user.getId());
    Objects.requireNonNull(user.getEmail());
    return new MailService.Mail(
        "the mailer", user.getEmail(), "your account", "your id: " + user.getId());
  }
}

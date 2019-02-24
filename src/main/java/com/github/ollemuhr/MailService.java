package com.github.ollemuhr;

import io.trane.future.Future;

/** Can send a mail. */
public interface MailService {

  default Future<Void> send(final Mail mail) {
    return Future.<Void>apply(
            () -> {
              print("pretending to send an email:");
              print("from:\t\t%s", mail.getFrom());
              print("to:\t\t\t%s", mail.getTo());
              print("subject:\t%s", mail.getSubject());
              print("message:\t%s", mail.getMessage());
              return null;
            })
        .onFailure(Throwable::printStackTrace)
        .rescue(e -> Future.VOID);
  }

  private static void print(final String s, final String... args) {
    System.out.println(String.format(s, args));
  }

  class Mail {

    private final String from;
    private final String to;
    private final String subject;
    private final String message;

    public Mail(final String from, final String to, final String subject, final String message) {
      this.from = from;
      this.to = to;
      this.subject = subject;
      this.message = message;
    }

    public String getFrom() {
      return from;
    }

    public String getTo() {
      return to;
    }

    public String getSubject() {
      return subject;
    }

    public String getMessage() {
      return message;
    }
  }
}

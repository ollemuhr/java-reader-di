package com.github.ollemuhr;

/**
 *
 */
public class Mail {
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

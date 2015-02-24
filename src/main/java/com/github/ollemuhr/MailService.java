package com.github.ollemuhr;

/**
 *
 */
public interface MailService {
    Void send(String from, String to, String subject, String message);
}

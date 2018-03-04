package com.github.ollemuhr;

/**
 *
 */
public interface Config {
    UserRepository getUserRepository();

    MailService getMailService();
}

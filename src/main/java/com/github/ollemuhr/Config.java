package com.github.ollemuhr;

/**
 * App configuration providing the implementations.
 */
public interface Config {
    UserRepository getUserRepository();

    MailService getMailService();
}

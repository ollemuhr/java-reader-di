package com.github.ollemuhr;

/** Signal problems regarding user existence in db. */
public class UserExistsException extends RuntimeException {

  /**
   * Instantiate.
   *
   * @param message the error message.
   */
  public UserExistsException(final String message) {
    super(message);
  }
}

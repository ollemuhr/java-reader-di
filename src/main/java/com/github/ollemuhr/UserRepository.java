package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;

/** The user db access. */
public interface UserRepository {

  /**
   * Get a user by id.
   *
   * @param id the id.
   * @return the user.
   */
  Option<User> get(Integer id);

  /**
   * Find a user by username.
   *
   * @param username the username.
   * @return the user.
   */
  Option<User> find(String username);

  /**
   * Store a user in db.
   *
   * @param user the user.
   * @return the stored user.
   */
  Validation<Seq<String>, User> create(User user);

  /**
   * Update a user.
   *
   * @param user the updated user.
   * @return the updated user.
   */
  Validation<Seq<String>, User> update(User user);
}

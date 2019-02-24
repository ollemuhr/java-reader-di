package com.github.ollemuhr;

import io.trane.future.Future;
import java.util.List;
import java.util.Optional;

/** The user db access. @author Olle Muhr | olle.muhr@fareoffice.com */
public interface UserRepository {

  /**
   * Get a user by id.
   *
   * @param id the id.
   * @return the user.
   */
  Future<Optional<User>> get(Long id);

  /**
   * Find a user by username.
   *
   * @param username the username.
   * @return the user.
   */
  Future<Optional<User>> find(String username);

  /**
   * Store a user in db.
   *
   * @param user the user.
   * @return the stored user.
   */
  Future<User> create(User user);

  /**
   * Update a user.
   *
   * @param user the updated user.
   * @return the updated user.
   */
  Future<User> update(User user);

  /**
   * Find all users.
   *
   * @return the future list of users.
   */
  Future<List<User>> findAll();
}

package com.github.ollemuhr;

import java.util.Optional;

/**
 *
 */
public interface UserRepository {
    User get(Integer id);
    Optional<User> getOpt(Integer id);
    User find(String username);
    Optional<User> findOpt(String username);
    User create(User user);
    Void update(User user);
}

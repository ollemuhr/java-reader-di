package com.github.ollemuhr;

import io.trane.future.Future;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class UserManager {

  private final Users users;

  private BiFunction<User, Optional<User>, Map<String, String>> toMap =
      (user, boss) -> {
        final Map<String, String> m = new HashMap<>();
        m.put("fullname", user.getFirstName() + " " + user.getLastName());
        m.put("email", user.getEmail());
        final String bossName =
            boss.map(b -> b.getFirstName() + " " + b.getLastName()).orElse("No boss");
        m.put("boss", bossName);
        return m;
      };

  public UserManager(final Users users) {
    this.users = users;
  }

  /**
   * User info.
   *
   * @param username the username.
   * @return some user info.
   */
  public Future<Optional<Map<String, String>>> userInfo(final String username) {
    final var userFuture = users.findUser(username);
    final var supervisorId = userFuture.map(ou -> ou.map(User::getSupervisorId));
    final var boss =
        supervisorId.flatMap(
            idOption -> idOption.map(users::getUser).orElse(Future.value(Optional.empty())));

    return userFuture.flatMap(ou -> boss.map(ob -> ou.map(u -> toMap.apply(u, ob))));
  }

  /**
   * Returns the boss.
   *
   * @param username the user with a boss.
   * @return the boss.
   */
  public Future<Optional<User>> findBossOf(final String username) {
    return users
        .findUser(username)
        .map(userOption -> userOption.map(User::getSupervisorId))
        .map(idOption -> idOption.map(users::getUser))
        .flatMap(a -> a.orElse(Future.value(Optional.empty())));
  }
}

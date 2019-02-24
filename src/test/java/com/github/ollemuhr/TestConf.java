package com.github.ollemuhr;

import static com.github.ollemuhr.UserTest.u1;
import static com.github.ollemuhr.UserTest.u2;

import io.trane.future.Future;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** Fake implementations of UserRepository and MailService. */
class TestConf {

  private static final AtomicLong idGen = new AtomicLong(u2.getId());

  private static final Map<Long, User> byId = init();

  private static Map<Long, User> init() {
    final Map<Long, User> m = new HashMap<>();
    m.put(u1.getId(), u1);
    m.put(u2.getId(), u2);
    return m;
  }

  Config config() {
    return new Config() {
      private UserRepository userRepository =
          new UserRepository() {
            @Override
            public Future<Optional<User>> get(final Long id) {
              return Future.value(Optional.ofNullable(byId.get(id)));
            }

            @Override
            public Future<Optional<User>> find(final String username) {
              return Future.value(findInMap(username));
            }

            private Optional<User> findInMap(final String username) {
              return byId.values()
                  .stream()
                  .filter(u -> u.getUsername().equals(username))
                  .findFirst();
            }

            @Override
            public Future<User> create(final User user) {
              return Future.apply(() -> store(user))
                  .rescue(
                      e -> {
                        if (e instanceof UserExistsException) {
                          return Future.exception(ValidationError.single(e.getMessage()));
                        }
                        return Future.exception(e);
                      });
            }

            @Override
            public Future<User> update(final User user) {
              return Future.apply(() -> tryUpdate(user))
                  .rescue(
                      e -> {
                        if (e instanceof UserExistsException) {
                          return Future.exception(ValidationError.single(e.getMessage()));
                        }
                        return Future.exception(e);
                      });
            }

            @Override
            public Future<List<User>> findAll() {
              return Future.apply(byId::values).map(ArrayList::new);
            }

            private User tryUpdate(final User user) {
              return Optional.ofNullable(byId.get(user.getId()))
                  .map(putUser(user))
                  .orElseThrow(() -> new UserExistsException("user.not.exists"));
            }

            private Function<User, User> putUser(final User user) {
              return __ -> {
                byId.put(user.getId(), user);
                return user;
              };
            }

            private User store(final User user) {
              if (findInMap(user.getUsername()).isPresent()) {
                throw new UserExistsException("user.unique.constraint");
              }
              if ("boomer".equals(user.getUsername())) {
                throw new RuntimeException("boom");
              }
              final User toInsert = user.withId(idGen.incrementAndGet());
              byId.put(toInsert.getId(), toInsert);
              return toInsert;
            }
          };

      private MailService mailService = new MailService() {};

      @Override
      public UserRepository getUserRepository() {
        return userRepository;
      }

      @Override
      public MailService getMailService() {
        return mailService;
      }
    };
  }
}

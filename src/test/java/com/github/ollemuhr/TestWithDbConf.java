package com.github.ollemuhr;

import io.trane.future.CheckedFutureException;
import io.trane.future.Future;
import io.trane.ndbc.DataSource;
import io.trane.ndbc.NdbcException;
import io.trane.ndbc.PreparedStatement;
import io.trane.ndbc.Row;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Fake implementations of UserRepository and MailService. */
class TestWithDbConf {

  private DataSource<PreparedStatement, Row> ds;

  TestWithDbConf() {
    final var config =
        io.trane.ndbc.Config.create(
                "io.trane.ndbc.postgres.netty4.DataSourceSupplier", "localhost", 0, "user")
            .database("test_schema")
            .password("test")
            .embedded("io.trane.ndbc.postgres.embedded.EmbeddedSupplier");

    // Create a DataSource
    ds = DataSource.fromConfig(config);
    // Define a timeout
    final var timeout = Duration.ofSeconds(1);

    try {
      ds.execute(
              "CREATE TABLE users(\n"
                  + " id serial PRIMARY KEY,\n"
                  + " username VARCHAR (50) UNIQUE NOT NULL,\n"
                  + " firstname VARCHAR (50) NOT NULL,\n"
                  + " lastname VARCHAR (50) NOT NULL,\n"
                  + " email VARCHAR (355) UNIQUE NOT NULL,\n"
                  + " supervisor_id int4 references users(id)"
                  + ");")
          .flatMap(__ -> ds.query(DB.insertUser(UserTest.u1)))
          .flatMap(__ -> ds.query(DB.insertUser(UserTest.u2)))
          .get(timeout);
    } catch (CheckedFutureException e) {
      throw new RuntimeException(e);
    }
  }

  Config config() {
    return new Config() {
      private UserRepository userRepository =
          new UserRepository() {
            @Override
            public Future<Optional<User>> get(final Long id) {
              return ds.query(DB.userById.setLong(id)).map(DB::mapUser);
            }

            @Override
            public Future<Optional<User>> find(final String username) {
              return ds.query(DB.userByUsername.setString(username)).map(DB::mapUser);
            }

            @Override
            public Future<User> create(final User user) {
              final var ps =
                  "boomer".equals(user.getUsername())
                      ? DB.insertUserWithBug(user)
                      : DB.insertUser(user);

              return ds.transactional(() -> ds.query(ps))
                  .map(DB::mapUser)
                  .map(Optional::orElseThrow)
                  .rescue(DB.handleUserExists());
            }

            @Override
            public Future<User> update(final User user) {
              return ds.execute(DB.updateUser(user))
                  .map(
                      n -> {
                        if (n == 0) {
                          throw ValidationError.single("user.not.exists");
                        }
                        return user;
                      });
            }

            @Override
            public Future<List<User>> findAll() {
              return ds.query(DB.allUsers).map(DB::mapUsers);
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

  private interface DB {

    PreparedStatement userById = PreparedStatement.create("select * from users where id=?;");
    PreparedStatement userByUsername =
        PreparedStatement.create("SELECT * FROM users where username=?;");
    PreparedStatement allUsers = PreparedStatement.create("SELECT * FROM users;");

    PreparedStatement insertUser =
        PreparedStatement.create(
            "INSERT INTO users(username, firstname, lastname, email, supervisor_id) values(?,?,?,?,?) RETURNING *;");
    PreparedStatement insertUserWithFail =
        PreparedStatement.create(
            "INSERT INTO users(boom, firstname, lastname, email, supervisor_id) values(?,?,?,?,?);");
    PreparedStatement updateUser =
        PreparedStatement.create(
            "UPDATE users SET username=?, firstname=?, lastname=?, email=?, supervisor_id=? WHERE id=?;");

    static Function<Throwable, Future<User>> handleUserExists() {
      return e -> {
        if (e instanceof NdbcException && e.getMessage().contains("users_username_key")) {
          return Future.exception(ValidationError.single("user.unique.constraint"));
        } else {
          return Future.exception(e);
        }
      };
    }

    static Optional<User> mapUser(final List<Row> rows) {
      return mapUsers(rows).stream().findFirst();
    }

    static List<User> mapUsers(final List<Row> rows) {
      return rows.stream()
          .map(
              row ->
                  User.valid(
                      row.getLong("id"),
                      row.isNull("supervisor_id") ? null : row.getLong("supervisor_id"),
                      row.getString("firstname"),
                      row.getString("lastname"),
                      row.getString("email"),
                      row.getString("username")))
          .collect(Collectors.toList());
    }

    static PreparedStatement insertUser(final User u) {
      return params(insertUser, u, false);
    }

    static PreparedStatement insertUserWithBug(final User u) {
      return params(insertUserWithFail, u, false);
    }

    static PreparedStatement updateUser(final User u) {
      return params(updateUser, u, true);
    }

    private static PreparedStatement params(
        final PreparedStatement ps, final User u, final boolean addId) {
      final var ret =
          ps.setString(0, u.getUsername())
              .setString(1, u.getFirstName())
              .setString(2, u.getLastName())
              .setString(3, u.getEmail())
              .setLong(4, u.getSupervisorId());
      return addId ? ret.setLong(5, u.getId()) : ret;
    }
  }
}

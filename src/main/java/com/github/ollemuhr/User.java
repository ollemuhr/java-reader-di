package com.github.ollemuhr;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import java.util.Objects;
import java.util.regex.Pattern;

/** A user that can only be created using static 'valid' method. */
public class User {

  private final Integer id;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final Integer supervisorId;
  private final String username;

  public static Validation<Seq<String>, User> valid(
      final Integer id,
      final Integer supervisorId,
      final String firstName,
      final String lastName,
      final String email,
      final String username) {
    return Valid.user(id, supervisorId, firstName, lastName, email, username);
  }

  private User(
      final Integer id,
      final Integer supervisorId,
      final String firstName,
      final String lastName,
      final String email,
      final String username) {
    this.id = id;
    this.supervisorId = supervisorId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.username = username;
  }

  /**
   * A user with id.
   *
   * @param id the id.
   * @return the copied user with an id.
   */
  public User withId(final Integer id) {
    return new User(
        id,
        this.getSupervisorId(),
        this.getFirstName(),
        this.getLastName(),
        this.getEmail(),
        this.getUsername());
  }

  public String getEmail() {
    return email;
  }

  public Integer getSupervisorId() {
    return supervisorId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getUsername() {
    return username;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, email, supervisorId, username);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final User other = (User) obj;
    return Objects.equals(this.id, other.id)
        && Objects.equals(this.firstName, other.firstName)
        && Objects.equals(this.lastName, other.lastName)
        && Objects.equals(this.email, other.email)
        && Objects.equals(this.supervisorId, other.supervisorId)
        && Objects.equals(this.username, other.username);
  }

  @Override
  public String toString() {
    return "User{"
        + "id="
        + id
        + ", firstName='"
        + firstName
        + '\''
        + ", lastName='"
        + lastName
        + '\''
        + ", email='"
        + email
        + '\''
        + ", supervisorId="
        + supervisorId
        + ", username='"
        + username
        + '\''
        + '}';
  }

  /** Some silly validation for a User. */
  private interface Valid {

    Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    Pattern namePattern = Pattern.compile("^[A-Za-z0-9+_.-]+$");

    static Validation<Seq<String>, User> user(
        final Integer id,
        final Integer supervisorId,
        final String firstName,
        final String lastName,
        final String email,
        final String username) {
      return Validation.<String, Integer>valid(id)
          .combine(Validation.valid(supervisorId))
          .combine(validFirstName(firstName))
          .combine(validLastName(lastName))
          .combine(validEmail(email))
          .combine(validUsername(username))
          .ap(User::new);
    }

    static Validation<String, String> validFirstName(final String s) {
      return namePattern.matcher(s).matches()
          ? Validation.valid(s)
          : Validation.invalid("user.firstName.invalid");
    }

    static Validation<String, String> validLastName(final String s) {
      return namePattern.matcher(s).matches()
          ? Validation.valid(s)
          : Validation.invalid("user.lastName.invalid");
    }

    static Validation<String, String> validEmail(final String s) {
      return emailPattern.matcher(s).matches()
          ? Validation.valid(s)
          : Validation.invalid("user.email.invalid");
    }

    static Validation<String, String> validUsername(final String s) {
      return s == null || s.length() < 4
          ? Validation.invalid("user.username.minlen")
          : Validation.valid(s);
    }
  }
}

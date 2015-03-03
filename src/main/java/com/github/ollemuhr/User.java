package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.github.ollemuhr.validation.Validation.failure;
import static com.github.ollemuhr.validation.Validation.success;

/**
 *
 */
public class User {

    private final Integer id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Integer supervisorId;
    private final String username;

    public User(
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

    private static Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static Pattern namePattern = Pattern.compile("^[A-Za-z0-9+_.-]+$");

    public static Validation<List<Object>, User> validate(final User user) {
        return success(user).failList()
                .accumulate(User::validEmail)
                .accumulate(User::validFirstName)
                .accumulate(User::validLastName)
                .accumulate(User::validUsername);

    }

    public static Validation<String, User> validFirstName(final User u) {
        return namePattern.matcher(u.getFirstName()).matches() ?
                success(u) : failure("user.firstname.invalid", u);
    }

    public static Validation<String, User> validLastName(final User u) {
        return namePattern.matcher(u.getLastName()).matches() ?
                success(u) : failure("user.lastname.invalid", u);
    }

    public static Validation<String, User> validEmail(final User u) {
        return emailPattern.matcher(u.getEmail()).matches() ?
                success(u) :failure("user.email.invalid", u);
    }

    public static Validation<String, User> validUsername(final User u) {
        return u.getUsername().length() > 4 ?
                success(u) :
                failure("user.username.minlen", u);
    }
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", supervisorId=" + supervisorId +
                ", username='" + username + '\'' +
                '}';
    }
}

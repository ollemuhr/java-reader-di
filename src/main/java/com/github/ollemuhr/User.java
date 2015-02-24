package com.github.ollemuhr;

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

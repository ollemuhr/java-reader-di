package com.github.ollemuhr;

import com.github.ollemuhr.validation.Validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class TestAppl {

    private final Config config;

    private final UserManager userManager = new UserManager();

    public TestAppl(final Config config) {
        this.config = config;
    }

    public User findById(final Integer id) {
        return run(userManager.getUser(id));
    }

    public User findByUsername(final String username) {
        return run(userManager.findUser(username));
    }

    Optional<String> getUserMail(final Integer id) {
        return run(userManager.userEmail(id));
    }

    public Map<String, String> getUserInfo(final String username) {
        return run(userManager.userInfo(username));
    }

    public Optional<User> findByIdOpt(final Integer id) {
        return run(userManager.getUserOpt(id));
    }

    public Optional<User> findByUserNameOpt(final String username) {
        return run(userManager.findUserOpt(username));
    }

    public Optional<Map<String, String>> getUserInfoOpt(final String username) {
        return run(userManager.userInfoOpt(username));
    }

    public User create(final User user) {
        return run(userManager.create(user));
    }

    public Validation<String, User> update(final User user) {
        return run(userManager.update(user));
    }

    public Validation<List<Object>, User> createValidAndMail(final User user) {

        return run(userManager.createValidAndMail(user));
    }

    private <T> T run(Reader<Config, T> r) {
        return r.apply(config);
    }
}

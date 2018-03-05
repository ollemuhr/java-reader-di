package com.github.ollemuhr;

import java.util.function.Function;

/**
 * A 'reader monad'.
 */
public class Configured<C, A> {

    private final Function<C, A> run;

    public Configured(final Function<C, A> run) {
        this.run = run;
    }

    public A apply(final C c) {
        return run.apply(c);
    }

    public <B> Configured<C, B> map(Function<? super A, ? extends B> f) {
        return new Configured<>((C c) -> f.apply(apply(c)));
    }

    public <B> Configured<C, B> flatMap(final Function<? super A, ? extends Configured<C, ? extends B>> f) {
        return new Configured<>(c -> f.apply(apply(c)).apply(c));
    }
}

package com.github.ollemuhr;

import java.util.function.Function;

/**
 *
 */
public class Reader<C, A> {

    private Function<C, A> run;

    public Reader(Function<C, A> run) {
        this.run = run;
    }

    public A apply(C c) {
        return run.apply(c);
    }

    public <B> Reader<C, B> map(Function<A, B> f) {
        return new Reader<>(c -> f.apply(run.apply(c)));
    }

    public <B> Reader<C, B> flatMap(Function<A, Reader<C, B>> f) {
        return new Reader<>(c -> f.apply(run.apply(c)).apply(c));
    }
}

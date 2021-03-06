package com.github.ollemuhr;

import java.util.function.Function;

/**
 * A 'reader monad'.
 *
 * <p>Inspiration: https://gist.github.com/danhyun/fda27d5682b7dbed151b
 *
 * <p>In Kotlin:
 *
 * <p>https://medium.com/@JorgeCastilloPr/kotlin-dependency-injection-with-the-reader-monad-7d52f94a482e
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

  public <B> Configured<C, B> flatMap(
      final Function<? super A, ? extends Configured<C, ? extends B>> f) {
    return new Configured<>(c -> f.apply(apply(c)).apply(c));
  }
}

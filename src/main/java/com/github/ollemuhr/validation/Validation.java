package com.github.ollemuhr.validation;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public abstract class Validation<L, A> {

    final A value;

    private Validation(A value) {
        this.value = value;
    }

    public abstract <B> Validation<L, B> map(Function<? super A, ? extends B> mapper);

    public abstract <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper);

    public abstract <B> Validation<L, B> accumulate(Function<? super A, Validation<?, ? extends B>> mapper);

    public abstract boolean isSuccess();

    public abstract L failure();

    public A value() {
        return value;
    }

    public static <L, A> Success<L, A> success(A value) {
        return new Success<L, A>(value);
    }

    public static <L, A> Failure<L, A> failure(L left, A value) {
        return new Failure<>(left, value);
    }

    public static class Success<L, A> extends Validation<L, A> {

        private Success(A value) {
            super(value);
        }

        @Override
        public <B> Validation<L, B> map(Function<? super A, ? extends B> mapper) {

            return success(mapper.apply(value));
        }

        @Override
        public <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {

            return (Validation<L, B>) mapper.apply(value);
        }

        @Override
        public <B> Validation<L, B> accumulate(Function<? super A, Validation<?, ? extends B>> mapper) {
            throw new NotImplementedException();
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public L failure() {
            return null;
        }

        @Override
        public String toString() {
            return "Success( " + value + " )";
        }

        public Success<List<L>, A> failList() {
            return new SuccessList<>(value);
        }
    }

    public static final class SuccessList<L, A> extends Success<List<L>, A> {

        public SuccessList(A value) {
            super(value);
        }

        @Override
        public <B> Validation<List<L>, B> map(Function<? super A, ? extends B> mapper) {

            return new SuccessList<>(mapper.apply(value));
        }

        @Override
        public <B> Validation<List<L>, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {

            return accumulate(mapper);
        }

        public <B> Validation<List<L>, B> accumulate(Function<? super A, Validation<?, ? extends B>> mapper) {
            Validation<?, ? extends B> result = mapper.apply(value);

            return  (result.isSuccess() ?
                    new SuccessList<>(result.value) :
                    new FailureList<>(((Failure<L, B>) result).left, result.value));

        }

        @Override
        public List<L> failure() {
            return null;
        }

    }

    public static class Failure<L, A> extends Validation<L, A> {

        protected final L left;


        public Failure(L left, A value) {
            super(value);
            this.left = left;
        }

        @Override
        public <B> Validation<L, B> map(Function<? super A, ? extends B> mapper) {

            return failure(left, (B) value);
        }

        @Override
        public <B> Validation<L, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {

            return failure(left, (B) value);
        }

        @Override
        public <B> Validation<L, B> accumulate(Function<? super A, Validation<?, ? extends B>> mapper) {
            Validation<?, ? extends B> result = mapper.apply(value);

            return result.isSuccess() ?
                    failure(left, result.value) :
                    failure(((Failure<L, B>) result).left, result.value);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public L failure() {
            return left;
        }

        @Override
        public String toString() {
            return "Failure( " + left + " )";
        }
    }

    public static final class FailureList<L, A> extends Failure<List<L>, A> {

        public FailureList(L left, A value) {
            super(new ArrayList<L>() {{
                add(left);
            }}, value);
        }

        private FailureList(List<L> left, A value) {
            super(left, value);
        }

        @Override
        public <B> Validation<List<L>, B> map(Function<? super A, ? extends B> mapper) {

            return new FailureList<>(left, (B) value);
        }

        @Override
        public <B> Validation<List<L>, B> flatMap(Function<? super A, Validation<?, ? extends B>> mapper) {
            return new FailureList<>(left, (B) value);
        }

        @Override
        public <B> Validation<List<L>, B> accumulate(Function<? super A, Validation<?, ? extends B>> mapper) {
            Validation<?, ? extends B> result = mapper.apply(value);

            Validation<List<L>, B> ret = result.isSuccess() ?
                    new FailureList<>(left, result.value) :
                    new FailureList<>(new ArrayList<L>(left) {{
                        add(((Failure<L, B>)result).left);
                    }}, result.value);

            return ret;
        }

    }
}
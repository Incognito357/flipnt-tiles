package com.incognito.acejam0.utils;

import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

public class Builder<T> {
    private final T t;

    public Builder(T t) {
        this.t = t;
    }

    public <A> Builder<T> with(BiConsumer<T, A> setter, A value) {
        setter.accept(t, value);
        return this;
    }

    public T build() {
        return t;
    }

    public <A, B> Builder<T> with(TriConsumer<T, A, B> setter, A a, B b) {
        setter.accept(t, a, b);
        return this;
    }
}

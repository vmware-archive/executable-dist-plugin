package io.pivotal.labs.io;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {

    public void accept(T t) throws E;

}

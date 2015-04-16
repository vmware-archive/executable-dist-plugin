package io.pivotal.labs.io;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {

    public T get() throws E;

}


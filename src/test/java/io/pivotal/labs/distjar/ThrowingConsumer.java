package io.pivotal.labs.distjar;

public interface ThrowingConsumer<T, E extends Exception> {

    void accept(T t) throws E;

}

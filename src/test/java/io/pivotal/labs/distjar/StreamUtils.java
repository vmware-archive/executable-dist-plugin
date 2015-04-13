package io.pivotal.labs.distjar;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtils {

    public static <T> Collector<T, ?, T> only() {
        return Collectors.collectingAndThen(Collectors.reducing((a, b) -> {
            throw new NoSuchElementException("Multiple values present: " + a + ", " + b);
        }), Optional::get);
    }

}

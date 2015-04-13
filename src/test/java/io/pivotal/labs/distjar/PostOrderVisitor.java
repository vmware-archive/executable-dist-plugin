package io.pivotal.labs.distjar;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PostOrderVisitor<T> implements FileVisitor<T> {

    public static <T> PostOrderVisitor<T> with(ThrowingConsumer<T, ? extends IOException> consumer) {
        return new PostOrderVisitor<T>(consumer);
    }

    private final ThrowingConsumer<T, ? extends IOException> consumer;

    private PostOrderVisitor(ThrowingConsumer<T, ? extends IOException> consumer) {
        this.consumer = consumer;
    }

    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
        consumer.accept(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
        consumer.accept(dir);
        return FileVisitResult.CONTINUE;
    }

}

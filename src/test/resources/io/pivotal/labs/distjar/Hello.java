package myapp;

import org.hamcrest.internal.ArrayIterator;

import java.util.Iterator;

public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, world!");
        Iterator<Object> arrayIterator = new ArrayIterator(args);
        while (arrayIterator.hasNext()) System.out.println(arrayIterator.next());
    }
}

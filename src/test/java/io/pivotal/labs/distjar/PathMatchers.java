package io.pivotal.labs.distjar;

import org.hamcrest.Matcher;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathMatchers {

    public static Matcher<Path> exists(Matcher<Boolean> matcher) {
        return MatcherUtils.featureMatcher("exists", Files::exists, matcher);
    }

}

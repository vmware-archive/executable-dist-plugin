package io.pivotal.labs.distjar;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Arrays;
import java.util.function.Function;

public class MatcherUtils {

    public static <T, U> Matcher<T> featureMatcher(String name, Function<T, U> feature, Matcher<? super U> matcher) {
        return new FeatureMatcher<T, U>(matcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return feature.apply(actual);
            }
        };
    }

    @SafeVarargs
    public static <T> Matcher<T> allOf(Matcher<? super T>... matchers) {
        return new TypeSafeDiagnosingMatcher<T>() {
            @Override
            protected boolean matchesSafely(T item, Description mismatchDescription) {
                boolean matches = true;
                for (Matcher<? super T> matcher : matchers) {
                    if (!matcher.matches(item)) {
                        if (!matches) mismatchDescription.appendText(", ");
                        matcher.describeMismatch(item, mismatchDescription);
                        matches = false;
                    }
                }
                return matches;
            }

            @Override
            public void describeTo(Description description) {
                description.appendList("(", " " + "and" + " ", ")", Arrays.asList(matchers));
            }
        };
    }

}

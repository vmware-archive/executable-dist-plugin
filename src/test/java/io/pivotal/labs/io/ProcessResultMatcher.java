package io.pivotal.labs.io;

import io.pivotal.labs.test.MatcherUtils;
import org.hamcrest.Matcher;

public class ProcessResultMatcher {

    public static Matcher<ProcessResult> hasExitValue(Matcher<? super Integer> matcher) {
        return MatcherUtils.featureMatcher("exitValue", ProcessResult::getExitValue, matcher);
    }

    public static Matcher<ProcessResult> hasOutput(Matcher<? super String> matcher) {
        return MatcherUtils.featureMatcher("output", ProcessResult::getOutput, matcher);
    }

    public static Matcher<ProcessResult> hasError(Matcher<? super String> matcher) {
        return MatcherUtils.featureMatcher("error", ProcessResult::getError, matcher);
    }

}

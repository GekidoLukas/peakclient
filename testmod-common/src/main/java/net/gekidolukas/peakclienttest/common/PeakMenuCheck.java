package net.gekidolukas.peakclienttest.common;

public record PeakMenuCheck(String instruction, boolean manual, Matcher matcher) {
    public enum Outcome { NO, YES, FAIL }

    @FunctionalInterface
    public interface Matcher {
        Outcome test(PeakMenuTestContext context, Object event);
    }
}

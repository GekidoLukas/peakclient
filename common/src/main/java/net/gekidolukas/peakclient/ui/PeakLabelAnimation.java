package net.gekidolukas.peakclient.ui;

public final class PeakLabelAnimation {
    private String lastTarget;
    private boolean counting;
    private double from;
    private double to;
    private double start;
    private double duration;
    private PeakLabelText.NumberSpan countSpan;

    private String revealTarget;
    private double revealStart;
    private int revealOffset;
    private boolean restartRequested;

    public String displayText(String target, int countUpTicks, double now) {
        if (countUpTicks <= 0 || lastTarget == null) {
            lastTarget = target;
            counting = false;
            return target;
        }
        if (!target.equals(lastTarget)) {
            PeakLabelText.NumberSpan newSpan = PeakLabelText.findNumber(target);
            PeakLabelText.NumberSpan oldSpan = PeakLabelText.findNumber(lastTarget);
            if (newSpan != null && oldSpan != null
                    && PeakLabelText.skeleton(target, newSpan).equals(PeakLabelText.skeleton(lastTarget, oldSpan))) {
                from = counting ? currentValue(now) : oldSpan.value();
                to = newSpan.value();
                start = now;
                duration = countUpTicks;
                countSpan = newSpan;
                counting = true;
            } else {
                counting = false;
            }
            lastTarget = target;
        }
        if (!counting) return target;
        double progress = (now - start) / duration;
        if (progress >= 1.0) {
            counting = false;
            return target;
        }
        return PeakLabelText.replaceNumber(target, countSpan, currentValue(now));
    }

    public int revealBudget(String target, float charsPerSecond, boolean countUpActive, double now) {
        if (charsPerSecond <= 0f) {
            revealTarget = target;
            return -1;
        }
        if (revealTarget == null || restartRequested) {
            revealTarget = target;
            revealStart = now;
            revealOffset = 0;
            restartRequested = false;
        } else if (!target.equals(revealTarget)) {
            if (countUpActive && sameApartFromNumber(target, revealTarget)) {
                revealTarget = target;
                int budgetKept = revealed(charsPerSecond, now);
                return budgetKept >= PeakLabelText.visibleLength(target) ? -1 : budgetKept;
            }
            int shown = revealed(charsPerSecond, now);
            if (target.startsWith(revealTarget) && shown >= PeakLabelText.visibleLength(revealTarget)) {
                revealOffset = PeakLabelText.visibleLength(revealTarget);
                revealStart = now;
            } else {
                revealOffset = 0;
                revealStart = now;
            }
            revealTarget = target;
        }
        int budget = revealed(charsPerSecond, now);
        return budget >= PeakLabelText.visibleLength(target) ? -1 : budget;
    }

    public void restart() {
        restartRequested = true;
    }

    public void reset() {
        lastTarget = null;
        counting = false;
        revealTarget = null;
        restartRequested = false;
    }

    private int revealed(float charsPerSecond, double now) {
        double elapsed = Math.max(0.0, now - revealStart);
        return revealOffset + (int) Math.floor(elapsed * charsPerSecond / 20.0);
    }

    private double currentValue(double now) {
        double progress = Math.max(0.0, Math.min(1.0, (now - start) / duration));
        double eased = 1.0 - Math.pow(1.0 - progress, 3.0);
        return from + (to - from) * eased;
    }

    private static boolean sameApartFromNumber(String first, String second) {
        PeakLabelText.NumberSpan firstSpan = PeakLabelText.findNumber(first);
        PeakLabelText.NumberSpan secondSpan = PeakLabelText.findNumber(second);
        return firstSpan != null && secondSpan != null
                && PeakLabelText.skeleton(first, firstSpan).equals(PeakLabelText.skeleton(second, secondSpan));
    }
}

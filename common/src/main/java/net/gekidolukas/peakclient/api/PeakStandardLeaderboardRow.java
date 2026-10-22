package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;

public final class PeakStandardLeaderboardRow<K> implements PeakLeaderboardRow<K> {
    private static final int DEFAULT_BACKGROUND = 0x90000000;

    private IntFunction<String> rankFormat = rank -> "#" + (rank + 1);
    private DoubleFunction<String> scoreFormat = PeakStandardLeaderboardRow::defaultScore;
    private Consumer<PeakPanel> style = panel -> {};
    private int rankWidth = 24;
    private int nameWidth = 90;
    private int scoreWidth = 40;

    public static <K> PeakStandardLeaderboardRow<K> create() {
        return new PeakStandardLeaderboardRow<>();
    }

    public static String defaultScore(double score) {
        if (score == Math.rint(score) && Math.abs(score) < 1e15) return String.valueOf((long) score);
        return String.format(Locale.ROOT, "%.1f", score);
    }

    public IntFunction<String> getRankFormat() { return rankFormat; }

    public PeakStandardLeaderboardRow<K> rankFormat(IntFunction<String> format) {
        this.rankFormat = format;
        return this;
    }

    public DoubleFunction<String> getScoreFormat() { return scoreFormat; }

    public PeakStandardLeaderboardRow<K> scoreFormat(DoubleFunction<String> format) {
        this.scoreFormat = format;
        return this;
    }

    public PeakStandardLeaderboardRow<K> style(Consumer<PeakPanel> style) {
        this.style = style;
        return this;
    }

    public PeakStandardLeaderboardRow<K> columns(int rankWidth, int nameWidth, int scoreWidth) {
        this.rankWidth = Math.max(1, rankWidth);
        this.nameWidth = Math.max(1, nameWidth);
        this.scoreWidth = Math.max(1, scoreWidth);
        return this;
    }

    public PeakPanel panel(String id, String rankText, String nameText, String scoreText) {
        return panel(id, rankText, Component.literal(nameText == null ? "" : nameText), scoreText);
    }

    public PeakPanel panel(String id, String rankText, Component nameText, String scoreText) {
        PeakPanel panel = PeakPanel.create(id).row().gap(4).padding(2, 4).background(DEFAULT_BACKGROUND);
        style.accept(panel);
        panel.add(PeakLabel.create(rankId(id), rankText).width(rankWidth),
                PeakLabel.create(nameId(id), nameText).width(nameWidth),
                PeakLabel.create(scoreId(id), scoreText).width(scoreWidth));
        return panel;
    }

    @Override
    public PeakElement<?> create(String rowId, PeakLeaderboardRanking.Placed<K> placed) {
        PeakLeaderboardRanking.Entry<K> entry = placed.entry();
        return panel(rowId, rankFormat.apply(placed.rank()), entry.name(), scoreFormat.apply(entry.score()));
    }

    @Override
    public void update(PeakView view, String rowId, PeakLeaderboardRanking.Placed<K> previous, PeakLeaderboardRanking.Placed<K> current) {
        if (previous.rank() != current.rank()) {
            view.update(rankId(rowId), PeakPatch.label().text(rankFormat.apply(current.rank())));
        }
        if (!previous.entry().name().equals(current.entry().name())) {
            view.update(nameId(rowId), PeakPatch.label().text(current.entry().name()));
        }
        if (previous.entry().score() != current.entry().score()) {
            view.update(scoreId(rowId), PeakPatch.label().text(scoreFormat.apply(current.entry().score())));
        }
    }

    public static String rankId(String rowId) { return rowId + "/rank"; }

    public static String nameId(String rowId) { return rowId + "/name"; }

    public static String scoreId(String rowId) { return rowId + "/score"; }
}

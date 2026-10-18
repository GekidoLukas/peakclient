package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakView;
import net.gekidolukas.peakclient.ui.PeakElement;

public interface PeakLeaderboardRow<K> {
    PeakElement<?> create(String rowId, PeakLeaderboardRanking.Placed<K> placed);

    void update(PeakView view, String rowId, PeakLeaderboardRanking.Placed<K> previous, PeakLeaderboardRanking.Placed<K> current);
}

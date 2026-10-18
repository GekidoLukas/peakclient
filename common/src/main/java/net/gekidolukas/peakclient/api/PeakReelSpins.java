package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PeakReelSpins {
    private PeakReelSpins() {}

    public static List<PeakUiOperation> operations(String reelId, PeakElement<?> winner, PeakElement<?> knownReel,
                                                   Random random) {
        List<PeakUiOperation> operations = new ArrayList<>(2);
        boolean present = false;
        Integer insertIndex = null;
        if (knownReel instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) {
                if (child.getId().equals(winner.getId())) {
                    present = true;
                    break;
                }
            }
            insertIndex = random.nextInt(container.getChildren().size() + 1);
        }
        if (!present) operations.add(new PeakUiOperation.Add(reelId, winner, insertIndex));
        operations.add(spinTo(reelId, winner.getId()));
        return operations;
    }

    public static PeakUiOperation spinTo(String reelId, String childId) {
        return new PeakUiOperation.Update(reelId, PeakPatch.reel().spinTo(childId).build().deepCopy());
    }

    public static boolean isReel(PeakElement<?> element) {
        return element instanceof PeakReel;
    }
}

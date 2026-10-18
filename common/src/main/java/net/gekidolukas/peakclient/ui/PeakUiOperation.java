package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

public sealed interface PeakUiOperation {

    record Update(String elementId, JsonObject patch) implements PeakUiOperation {}

    record UpdateByClass(String className, JsonObject patch) implements PeakUiOperation {}

    record Delete(String elementId) implements PeakUiOperation {}

    record Add(String parentId, PeakElement<?> element, Integer insertIndex) implements PeakUiOperation {
        public Add(String parentId, PeakElement<?> element) {
            this(parentId, element, null);
        }
    }
}

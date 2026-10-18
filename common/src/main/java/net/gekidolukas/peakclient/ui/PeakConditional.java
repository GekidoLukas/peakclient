package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.ArrayList;
import java.util.List;

public final class PeakConditional extends PeakContainer<PeakConditional> {
    private final List<PeakCondition> conditions = new ArrayList<>();
    private boolean requireAll = true; //true = AND; false = OR

    public PeakConditional() { this(PeakId.random()); }
    public PeakConditional(String id) { super(id); }
    public static PeakConditional create() { return new PeakConditional(); }
    public static PeakConditional create(String id) { return new PeakConditional(id); }
    public List<PeakCondition> getConditions() { return conditions; }
    public PeakConditional addCondition(PeakCondition condition) { conditions.add(condition); return this; }
    public boolean isRequireAll() { return requireAll; }
    public PeakConditional requireAll(boolean requireAll) { this.requireAll = requireAll; return this; }

    @Override
    public String elementType() { return "conditional"; }
}

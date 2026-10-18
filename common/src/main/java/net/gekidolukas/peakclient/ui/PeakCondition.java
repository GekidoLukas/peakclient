package net.gekidolukas.peakclient.ui;

public final class PeakCondition {
    private PeakConditionType type;
    private String stringValue;
    private double numberValue;

    private PeakCondition() {}

    private static PeakCondition of(PeakConditionType type) {
        PeakCondition condition = new PeakCondition();
        condition.type = type;
        return condition;
    }

    public static PeakCondition dimension(String dimensionId) { PeakCondition condition = of(PeakConditionType.DIMENSION); condition.stringValue = dimensionId; return condition; }
    public static PeakCondition gamemode(String gamemode) { PeakCondition condition = of(PeakConditionType.GAMEMODE); condition.stringValue = gamemode; return condition; }
    public static PeakCondition sneaking() { return of(PeakConditionType.SNEAKING); }
    public static PeakCondition sprinting() { return of(PeakConditionType.SPRINTING); }
    public static PeakCondition swimming() { return of(PeakConditionType.SWIMMING); }
    public static PeakCondition flying() { return of(PeakConditionType.FLYING); }
    public static PeakCondition riding() { return of(PeakConditionType.RIDING); }
    public static PeakCondition underwater() { return of(PeakConditionType.UNDERWATER); }
    public static PeakCondition isDay() { return of(PeakConditionType.IS_DAY); }
    public static PeakCondition isNight() { return of(PeakConditionType.IS_NIGHT); }
    public static PeakCondition healthBelow(double value) { PeakCondition condition = of(PeakConditionType.HEALTH_BELOW); condition.numberValue = value; return condition; }
    public static PeakCondition healthAbove(double value) { PeakCondition condition = of(PeakConditionType.HEALTH_ABOVE); condition.numberValue = value; return condition; }
    public static PeakCondition targetingEntity() { return of(PeakConditionType.TARGETING_ENTITY); }
    public static PeakCondition targetingBlock() { return of(PeakConditionType.TARGETING_BLOCK); }

    public PeakConditionType getType() { return type; }
    public String getStringValue() { return stringValue; }
    public double getNumberValue() { return numberValue; }
}

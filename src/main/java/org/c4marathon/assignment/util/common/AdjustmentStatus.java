package org.c4marathon.assignment.util.common;

public enum AdjustmentStatus {
    EQUAL(1, "1/N 정산"),
    RANDOM(2, "랜덤 정산");

    private final int value;
    private final String description;

    AdjustmentStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return this.value;
    }

    public String getDescription() {
        return this.description;
    }
}

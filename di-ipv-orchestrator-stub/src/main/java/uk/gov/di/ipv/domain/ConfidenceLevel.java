package uk.gov.di.ipv.domain;

public enum ConfidenceLevel {

    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    VERY_HIGH("VeryHigh");

    private final String name;

    ConfidenceLevel(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

package uk.gov.di.ipv.domain;

public enum Attribute {

    GIVEN_NAMES("givenNames"),
    SURNAME("surname"),
    ADDRESS("address"),
    PHONE_NUMBER("phoneNumber"),
    PASSPORT_NUMBER("passportNumber");

    private final String attributeName;

    Attribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}

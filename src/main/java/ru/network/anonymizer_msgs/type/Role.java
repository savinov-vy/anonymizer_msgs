package ru.network.anonymizer_msgs.type;

public enum Role {
    MAFIA, DON, CIVILIAN, SHERIFF;

    public boolean isBlack() {
        return this == MAFIA || this == DON;
    }
}

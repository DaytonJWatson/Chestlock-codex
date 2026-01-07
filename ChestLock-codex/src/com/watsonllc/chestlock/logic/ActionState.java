package com.watsonllc.chestlock.logic;

public class ActionState {
    private final boolean toggleEnabled;
    private final String target;
    private final long startedAtMillis;
    private final long expiresAtMillis;

    public ActionState(boolean toggleEnabled, String target, long startedAtMillis, long expiresAtMillis) {
        this.toggleEnabled = toggleEnabled;
        this.target = target;
        this.startedAtMillis = startedAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    public boolean isToggleEnabled() {
        return toggleEnabled;
    }

    public String getTarget() {
        return target;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }
}

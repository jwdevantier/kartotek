package it.defmacro.kartotek.jartotek.ui;

@FunctionalInterface
public interface StatusCallback {
    void call(String message, boolean error);
}

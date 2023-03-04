package it.defmacro.kartotek.jartotek.settings;

import java.nio.file.Path;

public class MissingConfException extends Exception {
    protected Path _confPath;

    public Path getConfPath() {
        return _confPath;
    }

    public MissingConfException(Path confPath) {
        super(String.format("missing config file at '%s'", confPath.toString()));
        this._confPath = confPath;
    }
}

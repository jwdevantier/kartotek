package it.defmacro.kartotek.jartotek.util;

import java.io.IOException;
import java.nio.file.Path;

public interface FileWriteFn {
    boolean run(Path target) throws IOException;
}

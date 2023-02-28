package it.defmacro.kartotek.jartotek.util;

import java.io.IOException;
import java.nio.file.*;

public class FUtils {
    public static boolean safeOverwrite(Path dst, FileWriteFn op) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile(dst.getParent(), "_note_tmp", ".tmp");
        } catch (IOException e) {
            return false;
        }
        boolean success = false;

        try{
            success = op.run(tmp);
        } catch (IOException ignored) {}

        if (success) {
            try {
                Files.move(tmp, dst, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return false;
            }
            return true;
        } else {  // failure
            try {
                if (tmp != null) {
                    Files.deleteIfExists(tmp);
                }
            } catch (IOException ignored) {}
            return false;
        }
    }
}

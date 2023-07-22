package io.github.rainyaphthyl.elytradashboard.util;

import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.UUID;

/**
 * Static methods
 */
public class GenericHelper {
    public static void copyFile(File src, File dest) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(src)) {
            try (FileOutputStream outputStream = new FileOutputStream(dest);
                 FileChannel inChannel = inputStream.getChannel();
                 FileChannel outChannel = outputStream.getChannel()) {
                outChannel.transferFrom(inChannel, 0, inChannel.size());
            }
        }
    }

    /**
     * {@link Objects#equals(Object, Object)} method by checking UUIDs
     */
    public static boolean equalsUnique(@Nullable Entity one, @Nullable Entity other) {
        if (one == other) return true;
        if (one == null || other == null) return false;
        UUID uuidOne = one.getUniqueID();
        UUID uuidOther = other.getUniqueID();
        return Objects.equals(uuidOne, uuidOther);
    }
}

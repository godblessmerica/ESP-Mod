package com.espmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class ConfigFiles {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Logger LOGGER = LoggerFactory.getLogger("espmod");

    static void save(Path path, Object data) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), "espmod-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Could not save ESP config {}", path, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LOGGER.warn("Could not remove temporary config {}", temporary, e);
                }
            }
        }
    }

    static void preserveInvalid(Path path, Exception cause) {
        LOGGER.error("Could not load ESP config {}; using defaults", path, cause);
        try {
            Files.copy(path, path.resolveSibling(path.getFileName() + ".invalid-" + System.nanoTime()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot preserve unreadable ESP config " + path, e);
        }
    }
}

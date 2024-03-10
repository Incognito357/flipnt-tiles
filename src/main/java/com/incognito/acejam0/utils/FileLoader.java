package com.incognito.acejam0.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.incognito.acejam0.domain.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileLoader {
    private static final Logger logger = LogManager.getLogger();

    private FileLoader() {}

    public static <T> T readFile(String file, Class<T> clazz) {
        try (InputStream in = new FileInputStream(file)) {
            return Mapper.getMapper().readValue(in, clazz);
        } catch (IOException e) {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
                if (in == null) {
                    logger.error("Could not load file {}", file);
                    return null;
                }
                return Mapper.getMapper().readValue(in, clazz);
            } catch (IOException e2) {
                logger.error("Could not load file {}", file, e2);
                return null;
            }
        }
    }

    public static <T> T readFile(String file, TypeReference<T> clazz) {
        try (InputStream in = new FileInputStream(file)) {
            return Mapper.getMapper().readValue(in, clazz);
        } catch (IOException e) {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
                if (in == null) {
                    logger.error("Could not load file {}", file);
                    return null;
                }
                return Mapper.getMapper().readValue(in, clazz);
            } catch (IOException e2) {
                logger.error("Could not load file {}", file, e2);
                return null;
            }
        }
    }
}

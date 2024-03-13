package com.incognito.acejam0.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
            logger.warn("Could not read file {} from system, attempting from jar... ({})", file, e.getMessage());
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
                if (in == null) {
                    logger.error("Could not load file {}, resource is null", file);
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
                logger.warn("Could not read file {} from system, attempting from jar... ({})", file, e.getMessage());
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

    public static BufferedImage getImage(String file) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                logger.error("Could not load image {}", file);
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            logger.error("Could not load image {}", file);
            return null;
        }
    }
}

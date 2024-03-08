package org.dmiit3iy.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
    public static String getMD5Hash(String fileName) throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(fileName))) {
            return DigestUtils.md5Hex(is);
        }
    }
}

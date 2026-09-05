package org.example.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SeedUtil {
    private SeedUtil() {
    }

    public static long toLong(String seedText) {
        try {
            // SHA-256 是确定性哈希；同一字符串总会得到同一组字节，从而复现随机序列。
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seedText.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
        }
    }
}

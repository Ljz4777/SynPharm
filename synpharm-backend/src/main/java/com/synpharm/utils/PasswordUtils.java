package com.synpharm.utils;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 */
@Component
public class PasswordUtils {

    private static final int SALT_LENGTH = 16;

    /**
     * 加密密码
     */
    public static String encrypt(String password) {
        try {
            // 生成盐
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // 结合盐和密码
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 组合盐和哈希密码
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verify(String password, String encryptedPassword) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPassword);

            // 提取盐
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // 提取哈希密码
            byte[] hashedPassword = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, hashedPassword, 0, hashedPassword.length);

            // 重新计算哈希
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] newHashedPassword = md.digest(password.getBytes());

            // 比较
            return MessageDigest.isEqual(hashedPassword, newHashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
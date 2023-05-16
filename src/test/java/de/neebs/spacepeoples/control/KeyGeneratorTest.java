package de.neebs.spacepeoples.control;

import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class KeyGeneratorTest {
    @Test
    void generateSecretKey() throws Exception {
        int n = 128;
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] rawData = secretKey.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(rawData);
        System.out.println(encodedKey);
    }

    @Test
    void generateSecretKey2() throws Exception {
        int keySize = 256;
        String password = "Admin123";
        byte[] salt = new byte[100];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, 1000, keySize);
        SecretKey pbeKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(pbeKeySpec);
        SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), "AES");
        byte[] rawData = secretKey.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(rawData);
        System.out.println(encodedKey);
    }
}

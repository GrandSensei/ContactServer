import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

//DEPRECIATED DONT USE ANYMORE

public class AESstuff {
        private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
        private static final int IV_SIZE = 16; // 16 bytes for AES

        // Generate a new 128-bit AES key.
        public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // 128-bit AES
            return keyGen.generateKey();
        }

        // Encrypts the plaintext and prepends the Base64-encoded IV (separated by a colon).
        public static String encrypt(String plainText, SecretKey key) throws Exception {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            // Generate a random IV.
            byte[] iv = new byte[IV_SIZE];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            // Return "Base64(iv):Base64(ciphertext)"
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        }

        // Decrypts the ciphertext. Expects the cipherText to be in the form "Base64(iv):Base64(ciphertext)".
        public static String decrypt(String cipherText, SecretKey key) throws Exception {
            String[] parts = cipherText.split(":");
            if(parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted format");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
    }


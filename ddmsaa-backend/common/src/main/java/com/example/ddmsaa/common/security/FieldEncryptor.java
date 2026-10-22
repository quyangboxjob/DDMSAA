package com.example.ddmsaa.common.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 項目単位の AES-256-GCM 暗号化・復号コンポーネント。
 *
 * <p>鍵は {@code app.crypto.aes-key}（Base64 の 32 バイト）。出力は Base64(IV 12 バイト +
 * 暗号文)。スレッドセーフ。
 */
@Component
public class FieldEncryptor {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private static final int KEY_LENGTH = 32;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Base64 エンコード済み 32 バイトの AES 鍵（app.crypto.aes-key / 環境変数 AES_KEY）。 */
    @Value("${app.crypto.aes-key:}")
    private String aesKey;

    /** Spring による生成用の既定コンストラクタ（鍵は {@code @Value} で注入される）。 */
    public FieldEncryptor() {
    }

    /**
     * 平文を暗号化する。呼び出しごとにランダムな IV を用いる。
     *
     * @param plaintext 平文（null・空はそのまま返す）
     * @return Base64(IV + 暗号文)
     * @throws IllegalStateException 鍵が未設定・不正、または暗号化に失敗した場合
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        SecretKeySpec key = resolveKey();
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, out, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, out, IV_LENGTH, cipherText.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * 暗号文を復号する。
     *
     * @param ciphertext {@link #encrypt(String)} が返した Base64 文字列（null・空はそのまま）
     * @return 平文
     * @throws IllegalArgumentException 暗号文が短すぎる、または Base64 として不正な場合
     * @throws IllegalStateException 鍵が未設定・不正、または復号（改ざん検知を含む）に失敗した場合
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        SecretKeySpec key = resolveKey();
        try {
            byte[] in = Base64.getDecoder().decode(ciphertext);
            if (in.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Invalid ciphertext");
            }
            byte[] iv = Arrays.copyOfRange(in, 0, IV_LENGTH);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(in, IV_LENGTH, in.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private SecretKeySpec resolveKey() {
        if (aesKey == null || aesKey.isBlank()) {
            throw new IllegalStateException("app.crypto.aes-key is not configured");
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(aesKey.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("app.crypto.aes-key is not valid Base64", e);
        }
        if (raw.length != KEY_LENGTH) {
            throw new IllegalStateException("app.crypto.aes-key must be 32 bytes (Base64)");
        }
        return new SecretKeySpec(raw, "AES");
    }
}

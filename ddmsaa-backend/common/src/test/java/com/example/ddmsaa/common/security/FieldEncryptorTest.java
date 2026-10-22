package com.example.ddmsaa.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("FieldEncryptor の AES-GCM 暗号化")
class FieldEncryptorTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    private static FieldEncryptor withKey(String key) {
        FieldEncryptor encryptor = new FieldEncryptor();
        ReflectionTestUtils.setField(encryptor, "aesKey", key);
        return encryptor;
    }

    @Test
    @DisplayName("暗号化して復号すると元の平文に戻る")
    void roundTrip() {
        FieldEncryptor encryptor = withKey(KEY);

        String encrypted = encryptor.encrypt("1234-5678 日本語");

        assertThat(encrypted).isNotEqualTo("1234-5678 日本語");
        assertThat(encryptor.decrypt(encrypted)).isEqualTo("1234-5678 日本語");
    }

    @Test
    @DisplayName("出力は Base64(iv 12 バイト + 暗号文 + 認証タグ 16 バイト)")
    void outputFormat() {
        byte[] raw = Base64.getDecoder().decode(withKey(KEY).encrypt("abc"));

        assertThat(raw).hasSize(12 + 3 + 16);
    }

    @Test
    @DisplayName("同じ平文でも呼び出しごとに異なる暗号文になる")
    void nonDeterministic() {
        FieldEncryptor encryptor = withKey(KEY);

        assertThat(encryptor.encrypt("same")).isNotEqualTo(encryptor.encrypt("same"));
    }

    @Test
    @DisplayName("null は null、空文字は空文字を返す")
    void nullAndEmpty() {
        FieldEncryptor encryptor = withKey(KEY);

        assertThat(encryptor.encrypt(null)).isNull();
        assertThat(encryptor.decrypt(null)).isNull();
        assertThat(encryptor.encrypt("")).isEmpty();
        assertThat(encryptor.decrypt("")).isEmpty();
    }

    @Test
    @DisplayName("鍵が未設定なら IllegalStateException")
    void missingKey() {
        FieldEncryptor unset = withKey("");
        FieldEncryptor nullKey = withKey(null);

        assertThatThrownBy(() -> unset.encrypt("x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> unset.decrypt("x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> nullKey.encrypt("x")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("鍵が 32 バイトでない・Base64 でないなら IllegalStateException")
    void invalidKey() {
        FieldEncryptor shortKey = withKey(Base64.getEncoder().encodeToString(new byte[16]));
        FieldEncryptor notBase64 = withKey("%%%not-base64%%%");

        assertThatThrownBy(() -> shortKey.encrypt("x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> notBase64.encrypt("x"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("改ざんされた暗号文の復号は IllegalStateException")
    void tamperedCiphertext() {
        FieldEncryptor encryptor = withKey(KEY);
        byte[] raw = Base64.getDecoder().decode(encryptor.encrypt("abc"));
        raw[raw.length - 1] ^= 0x01;
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThatThrownBy(() -> encryptor.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("別の鍵では復号できない")
    void wrongKey() {
        String encrypted = withKey(KEY).encrypt("abc");
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        FieldEncryptor other = withKey(Base64.getEncoder().encodeToString(otherKey));

        assertThatThrownBy(() -> other.decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }
}

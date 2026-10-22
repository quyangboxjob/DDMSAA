package com.example.ddmsaa.common.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("YamlMessageCatalog のメッセージ解決")
class YamlMessageCatalogTest {

    private YamlMessageCatalog catalog;

    private ClassLoader originalLoader;

    @BeforeEach
    void setUp() {
        originalLoader = Thread.currentThread().getContextClassLoader();
        catalog = new YamlMessageCatalog();
        catalog.init();
    }

    @AfterEach
    void tearDown() {
        Thread.currentThread().setContextClassLoader(originalLoader);
    }

    @Test
    @DisplayName("登録済み messageId は日本語正本の文言を返し exists は true")
    void resolveRegistered() {
        assertThat(catalog.resolve("E0008")).isEqualTo("入力内容に誤りがあります");
        assertThat(catalog.exists("E0008")).isTrue();
    }

    @Test
    @DisplayName("args があれば {0}・{1} を置換する")
    void resolveWithArgs() {
        assertThat(catalog.resolve("I0010", "U0001")).isEqualTo("登録しました（ID: U0001）");
        assertThat(catalog.resolve("E0032", "名前", 20)).isEqualTo("名前は20文字以内で入力してください");
    }

    @Test
    @DisplayName("言語バリアントは正本を上書きしない")
    void variantDoesNotOverrideCanonical() {
        assertThat(catalog.resolve("E0005")).isEqualTo("対象が見つかりません");
    }

    @Test
    @DisplayName("未登録の messageId はそのまま返し exists は false")
    void resolveUnknown() {
        assertThat(catalog.resolve("E9999")).isEqualTo("E9999");
        assertThat(catalog.exists("E9999")).isFalse();
    }

    @Test
    @DisplayName("messageId が null なら null を返し exists は false")
    void resolveNull() {
        assertThat(catalog.resolve(null)).isNull();
        assertThat(catalog.exists(null)).isFalse();
    }

    @Test
    @DisplayName("YAML が不正なら IllegalStateException で起動を停止する")
    void initFailsOnInvalidYaml(@TempDir Path dir) throws Exception {
        Path messages = Files.createDirectories(dir.resolve("messages"));
        Files.writeString(messages.resolve("broken.yaml"), "messages: [unclosed",
                StandardCharsets.UTF_8);
        useClasspath(dir);

        assertThatThrownBy(() -> new YamlMessageCatalog().init())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("messages キーが無い YAML は IllegalStateException")
    void initFailsOnMissingTopLevelKey(@TempDir Path dir) throws Exception {
        Path messages = Files.createDirectories(dir.resolve("messages"));
        Files.writeString(messages.resolve("other.yaml"), "foo: bar\n", StandardCharsets.UTF_8);
        useClasspath(dir);

        assertThatThrownBy(() -> new YamlMessageCatalog().init())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("別の YAML のメッセージも全件ロードされる")
    void loadsAdditionalFiles(@TempDir Path dir) throws Exception {
        Path messages = Files.createDirectories(dir.resolve("messages"));
        Files.writeString(messages.resolve("extra-messages.yaml"),
                "messages:\n  X0001: { category: INFO, message: \"追加 {0}\" }\n",
                StandardCharsets.UTF_8);
        useClasspath(dir);
        YamlMessageCatalog extended = new YamlMessageCatalog();

        extended.init();

        assertThat(extended.resolve("X0001", "a")).isEqualTo("追加 a");
        assertThat(extended.exists("E0008")).isTrue();
    }

    private static void useClasspath(Path dir) throws Exception {
        URL[] urls = {dir.toUri().toURL()};
        Thread.currentThread().setContextClassLoader(
                new URLClassLoader(urls, Thread.currentThread().getContextClassLoader()));
    }
}

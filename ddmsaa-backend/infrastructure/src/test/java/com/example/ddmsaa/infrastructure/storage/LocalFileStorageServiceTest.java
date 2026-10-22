package com.example.ddmsaa.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.common.exception.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("LocalFileStorageService のローカル保存")
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalFileStorageService(tempDir.toString());
    }

    private static ByteArrayInputStream bytes(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("upload は親ディレクトリを自動作成して保存し、download で読み戻せる")
    void uploadAndDownload() throws Exception {
        service.upload("order/OR00000001/a.txt", bytes("hello"), 5, "text/plain");

        assertThat(Files.exists(tempDir.resolve("order/OR00000001/a.txt"))).isTrue();
        try (InputStream in = service.download("order/OR00000001/a.txt")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello");
        }
    }

    @Test
    @DisplayName("同じキーへの upload は上書きする")
    void uploadOverwrites() throws Exception {
        service.upload("a.txt", bytes("one"), 3, "text/plain");
        service.upload("a.txt", bytes("two"), 3, "text/plain");

        try (InputStream in = service.download("a.txt")) {
            assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("two");
        }
    }

    @Test
    @DisplayName("upload の objectKey / content が null なら IllegalArgumentException")
    void uploadRejectsNull() {
        assertThatThrownBy(() -> service.upload(null, bytes("x"), 1, "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.upload("a.txt", null, 0, "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("存在しないファイルの download は EntityNotFoundException(E0005)")
    void downloadMissing() {
        assertThatThrownBy(() -> service.download("missing.txt"))
                .isInstanceOfSatisfying(EntityNotFoundException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0005"));
    }

    @Test
    @DisplayName("delete はファイルを削除し、存在しなくても例外にならない")
    void deleteIsIdempotent() {
        service.upload("a.txt", bytes("x"), 1, "text/plain");

        service.delete("a.txt");
        service.delete("a.txt");

        assertThat(Files.exists(tempDir.resolve("a.txt"))).isFalse();
    }

    @Test
    @DisplayName("presignedUrl は対象ファイルの file:// URI を返し、実体が無くても返す")
    void presignedUrlReturnsFileUri() {
        String url = service.presignedUrl("x/a.txt", 60);

        assertThat(url).startsWith("file:");
        assertThat(url).endsWith("/x/a.txt");
    }

    @Test
    @DisplayName("objectKey が null なら download / delete / presignedUrl は IllegalArgumentException")
    void nullKeys() {
        assertThatThrownBy(() -> service.download(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.presignedUrl(null, 60))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("base-dir の外を指す objectKey は拒否する")
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> service.upload("../evil.txt", bytes("x"), 1, "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.download("a/../../evil.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("IO 失敗は RuntimeException にラップされ E0005 にはならない")
    void ioFailureIsWrapped() throws Exception {
        Files.createDirectories(tempDir.resolve("dir.txt"));
        Path blocker = tempDir.resolve("blocker");
        Files.writeString(blocker, "file");

        assertThatThrownBy(() -> service.upload("blocker/child.txt", bytes("x"), 1, "text/plain"))
                .isInstanceOf(RuntimeException.class)
                .isNotInstanceOf(EntityNotFoundException.class);
    }
}

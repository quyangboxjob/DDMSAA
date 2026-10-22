package com.example.ddmsaa.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ddmsaa.common.exception.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@DisplayName("S3FileStorageService の S3 呼び出し（ネットワーク非依存）")
class S3FileStorageServiceTest {

    private S3Client client;

    private S3Presigner presigner;

    private S3FileStorageService service;

    @BeforeEach
    void setUp() {
        client = mock(S3Client.class);
        presigner = mock(S3Presigner.class);
        service = new S3FileStorageService(client, presigner, "bucket", "files", 300);
    }

    @Test
    @DisplayName("upload は接頭辞付きキーで PutObject を呼ぶ")
    void uploadPutsObject() {
        service.upload("a/b.txt", new ByteArrayInputStream(new byte[] {1, 2}), 2, "text/plain");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("bucket");
        assertThat(captor.getValue().key()).isEqualTo("files/a/b.txt");
        assertThat(captor.getValue().contentType()).isEqualTo("text/plain");
        assertThat(captor.getValue().contentLength()).isEqualTo(2);
    }

    @Test
    @DisplayName("接頭辞が空ならキーをそのまま使う")
    void emptyPrefixKeepsKey() {
        S3FileStorageService noPrefix = new S3FileStorageService(client, presigner, "bucket", "",
                300);

        noPrefix.delete("a.txt");

        ArgumentCaptor<DeleteObjectRequest> captor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(client).deleteObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("a.txt");
    }

    @Test
    @DisplayName("upload の objectKey / content が null なら IllegalArgumentException")
    void uploadRejectsNull() {
        assertThatThrownBy(
                () -> service.upload(null, new ByteArrayInputStream(new byte[0]), 0, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.upload("a", null, 0, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("upload の SDK 例外は RuntimeException にラップする")
    void uploadWrapsSdkException() {
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("down"));

        assertThatThrownBy(() -> service.upload("a", new ByteArrayInputStream(new byte[1]), 1, "x"))
                .isInstanceOf(RuntimeException.class)
                .isNotInstanceOf(EntityNotFoundException.class)
                .hasCauseInstanceOf(SdkClientException.class);
    }

    @Test
    @DisplayName("download の NoSuchKey は EntityNotFoundException(E0005)")
    void downloadNoSuchKey() {
        when(client.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("none").build());

        assertThatThrownBy(() -> service.download("a"))
                .isInstanceOfSatisfying(EntityNotFoundException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0005"));
    }

    @Test
    @DisplayName("download のその他の SDK 例外は E0005 にせず RuntimeException にラップする")
    void downloadWrapsOtherSdkException() {
        when(client.getObject(any(GetObjectRequest.class)))
                .thenThrow(SdkClientException.create("down"));

        assertThatThrownBy(() -> service.download("a"))
                .isInstanceOf(RuntimeException.class)
                .isNotInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("delete は存在しないキーでも正常終了する")
    void deleteIsIdempotent() {
        when(client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("none").build());

        service.delete("a");
    }

    @Test
    @DisplayName("presignedUrl は署名器で URL を生成し、指定秒数を有効期間にする")
    void presignedUrlUsesExpiry() throws Exception {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        URL url = URI.create("https://example.com/files/a?sig=1").toURL();
        when(presigned.url()).thenReturn(url);
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        String result = service.presignedUrl("a", 60);

        assertThat(result).isEqualTo("https://example.com/files/a?sig=1");
        ArgumentCaptor<GetObjectPresignRequest> captor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner).presignGetObject(captor.capture());
        assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofSeconds(60));
        assertThat(captor.getValue().getObjectRequest().key()).isEqualTo("files/a");
    }

    @Test
    @DisplayName("expirySeconds が 0 以下なら標準の有効期間を使う")
    void presignedUrlFallsBackToDefaultTtl() throws Exception {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(URI.create("https://example.com/x").toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        service.presignedUrl("a", 0);

        ArgumentCaptor<GetObjectPresignRequest> captor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner).presignGetObject(captor.capture());
        assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofSeconds(300));
    }

    @Test
    @DisplayName("presignedUrl の objectKey が null なら IllegalArgumentException")
    void presignedUrlRejectsNull() {
        assertThatThrownBy(() -> service.presignedUrl(null, 60))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("公開コンストラクタはネットワークに接続せずクライアントを構築できる")
    void publicConstructorBuildsClients() {
        S3FileStorageService built = new S3FileStorageService("ap-northeast-1", "bucket", "p",
                "http://localhost:9000", true, 300);

        built.close();
    }
}

package com.example.ddmsaa.infrastructure.storage;

import com.example.ddmsaa.common.exception.EntityNotFoundException;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * AWS S3（AWS SDK for Java v2）を保存先とする {@link FileStorageService} の実装。
 *
 * <p>{@code app.file.storage-type=s3} のとき有効（Local と排他）。endpoint / path-style の指定により
 * ローカル開発用 MinIO（S3 互換）にも接続できる。認証情報は AWS SDK の既定プロバイダチェーンに従う。
 * SDK 例外は RuntimeException でラップし、NoSuchKey のみ E0005 にマップする。</p>
 */
@Component
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "s3")
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    private final S3Presigner presigner;

    private final String bucket;

    private final String prefix;

    private final int presignTtlSeconds;

    /**
     * 接続設定から S3 クライアントと署名器を構築する。
     *
     * @param region リージョン
     * @param bucket バケット名
     * @param prefix オブジェクトキー接頭辞（null/空なら付けない）
     * @param endpoint S3 互換エンドポイント URL（null/空なら AWS 既定）
     * @param pathStyle パススタイルアクセスを使うか
     * @param presignTtlSeconds 署名付き URL の標準有効期間（秒）
     */
    @Autowired
    public S3FileStorageService(
            @Value("${app.file.s3.region:ap-northeast-1}") String region,
            @Value("${app.file.s3.bucket:ddmsaa}") String bucket,
            @Value("${app.file.s3.prefix:}") String prefix,
            @Value("${app.file.s3.endpoint:}") String endpoint,
            @Value("${app.file.s3.path-style:false}") boolean pathStyle,
            @Value("${app.file.s3.presign-ttl-seconds:300}") int presignTtlSeconds) {
        this(buildClient(region, endpoint, pathStyle), buildPresigner(region, endpoint, pathStyle),
                bucket, prefix, presignTtlSeconds);
    }

    /**
     * 構築済みのクライアントと署名器を注入する（テスト用）。
     */
    S3FileStorageService(S3Client s3Client, S3Presigner presigner, String bucket, String prefix,
            int presignTtlSeconds) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = bucket;
        this.prefix = normalizePrefix(prefix);
        this.presignTtlSeconds = presignTtlSeconds;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException S3 呼び出しが失敗した場合
     */
    @Override
    public void upload(String objectKey, InputStream content, long contentLength,
            String contentType) {
        if (objectKey == null || content == null) {
            throw new IllegalArgumentException("objectKey and content must not be null");
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fullKey(objectKey))
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(content, contentLength));
        } catch (SdkException e) {
            throw new RuntimeException("Failed to upload object: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException NoSuchKey 以外の S3 呼び出し失敗の場合
     */
    @Override
    public InputStream download(String objectKey) {
        if (objectKey == null) {
            throw new IllegalArgumentException("objectKey must not be null");
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fullKey(objectKey))
                .build();
        try {
            return s3Client.getObject(request);
        } catch (NoSuchKeyException e) {
            throw new EntityNotFoundException();
        } catch (SdkException e) {
            throw new RuntimeException("Failed to download object: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>S3 の DeleteObject は存在しないキーでも成功するため冪等である。</p>
     */
    @Override
    public void delete(String objectKey) {
        if (objectKey == null) {
            throw new IllegalArgumentException("objectKey must not be null");
        }
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fullKey(objectKey))
                .build();
        try {
            s3Client.deleteObject(request);
        } catch (NoSuchKeyException e) {
            return;
        } catch (SdkException e) {
            throw new RuntimeException("Failed to delete object: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>expirySeconds が 0 以下の場合は {@code app.file.s3.presign-ttl-seconds} を用いる。
     * 署名は URL 生成のみで、実体の存在確認は行わない。</p>
     */
    @Override
    public String presignedUrl(String objectKey, int expirySeconds) {
        if (objectKey == null) {
            throw new IllegalArgumentException("objectKey must not be null");
        }
        int seconds = expirySeconds > 0 ? expirySeconds : presignTtlSeconds;
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(seconds))
                .getObjectRequest(r -> r.bucket(bucket).key(fullKey(objectKey)))
                .build();
        try {
            return presigner.presignGetObject(request).url().toString();
        } catch (SdkException e) {
            throw new RuntimeException("Failed to presign object: " + objectKey, e);
        }
    }

    /**
     * S3 クライアントと署名器を解放する。
     */
    @PreDestroy
    void close() {
        s3Client.close();
        presigner.close();
    }

    private String fullKey(String objectKey) {
        return prefix + objectKey;
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private static S3Client buildClient(String region, String endpoint, boolean pathStyle) {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    private static S3Presigner buildPresigner(String region, String endpoint, boolean pathStyle) {
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}

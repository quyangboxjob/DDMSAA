package com.example.ddmsaa.infrastructure.storage;

import com.example.ddmsaa.common.exception.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ローカルファイルシステムを保存先とする {@link FileStorageService} の既定実装。
 *
 * <p>{@code app.file.storage-type=local} または未設定のとき有効。保存先は
 * {@code app.file.local.base-dir}（既定 {@code ${java.io.tmpdir}/ddmsaa-files}）配下。
 * base-dir の外を指すオブジェクトキーは拒否する。</p>
 */
@Component
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "local",
        matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path baseDir;

    /**
     * 保存先ディレクトリを指定して生成する。
     *
     * @param baseDir 保存先のベースディレクトリ
     */
    public LocalFileStorageService(
            @Value("${app.file.local.base-dir:${java.io.tmpdir}/ddmsaa-files}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    /**
     * {@inheritDoc}
     *
     * <p>親ディレクトリは自動作成する。IO 例外は RuntimeException でラップして再送出する。</p>
     */
    @Override
    public void upload(String objectKey, InputStream content, long contentLength,
            String contentType) {
        if (objectKey == null || content == null) {
            throw new IllegalArgumentException("objectKey and content must not be null");
        }
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException objectKey が null の場合
     */
    @Override
    public InputStream download(String objectKey) {
        Path target = resolve(objectKey);
        if (!Files.isRegularFile(target)) {
            throw new EntityNotFoundException();
        }
        try {
            return Files.newInputStream(target);
        } catch (java.nio.file.NoSuchFileException e) {
            throw new EntityNotFoundException();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException objectKey が null の場合
     */
    @Override
    public void delete(String objectKey) {
        Path target = resolve(objectKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + objectKey, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>dev 向けに実署名は行わず、対象ファイルの URI（file://）を返す。expirySeconds は無視する。</p>
     */
    @Override
    public String presignedUrl(String objectKey, int expirySeconds) {
        return resolve(objectKey).toUri().toString();
    }

    private Path resolve(String objectKey) {
        if (objectKey == null) {
            throw new IllegalArgumentException("objectKey must not be null");
        }
        Path target = baseDir.resolve(objectKey).normalize();
        if (!target.startsWith(baseDir) || target.equals(baseDir)) {
            throw new IllegalArgumentException("Invalid objectKey: " + objectKey);
        }
        return target;
    }
}

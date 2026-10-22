package com.example.ddmsaa.core.support;

import com.example.ddmsaa.infrastructure.storage.FileStorageService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * テスト用のインメモリ {@link FileStorageService}。ファイルシステムや S3 に触れず、保存した
 * キーと内容をテストから確認できる。
 */
public class InMemoryFileStorageService implements FileStorageService {

    private final Map<String, byte[]> objects = new ConcurrentHashMap<>();

    private final Map<String, String> contentTypes = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void upload(String objectKey, InputStream content, long contentLength,
            String contentType) {
        try {
            objects.put(objectKey, content.readAllBytes());
            contentTypes.put(objectKey, contentType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public InputStream download(String objectKey) {
        byte[] bytes = objects.get(objectKey);
        if (bytes == null) {
            throw new NoSuchElementException("object not found: " + objectKey);
        }
        return new ByteArrayInputStream(bytes);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String objectKey) {
        objects.remove(objectKey);
        contentTypes.remove(objectKey);
    }

    /** {@inheritDoc} */
    @Override
    public String presignedUrl(String objectKey, int expirySeconds) {
        return "memory://" + objectKey + "?expires=" + expirySeconds;
    }

    /**
     * キーのオブジェクトが保存されているかを返す。
     *
     * @param objectKey オブジェクトキー
     * @return 保存されていれば true
     */
    public boolean contains(String objectKey) {
        return objects.containsKey(objectKey);
    }

    /**
     * 保存したオブジェクトの内容を返す。
     *
     * @param objectKey オブジェクトキー
     * @return 内容。未保存なら null
     */
    public byte[] content(String objectKey) {
        return objects.get(objectKey);
    }

    /**
     * 保存時に渡された Content-Type を返す。
     *
     * @param objectKey オブジェクトキー
     * @return Content-Type。未保存なら null
     */
    public String contentType(String objectKey) {
        return contentTypes.get(objectKey);
    }

    /**
     * 保存済みのキー一覧を返す。
     *
     * @return キーの集合（コピー）
     */
    public Set<String> keys() {
        return Set.copyOf(objects.keySet());
    }

    /** 保存内容をすべて消す。 */
    public void clear() {
        objects.clear();
        contentTypes.clear();
    }
}

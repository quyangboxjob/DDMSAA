package com.example.ddmsaa.core.support;

import com.example.ddmsaa.infrastructure.scan.ScanResult;
import com.example.ddmsaa.infrastructure.scan.VirusScanService;
import com.example.ddmsaa.infrastructure.scan.VirusScanUnavailableException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * テスト用の {@link VirusScanService}。結果（SAFE / REJECTED）と「スキャナ停止」をテストから
 * 切り替えられる。既定は SAFE。
 */
public class StubVirusScanService implements VirusScanService {

    private volatile ScanResult result = ScanResult.SAFE;

    private volatile boolean unavailable;

    private final AtomicInteger scanCount = new AtomicInteger();

    /** {@inheritDoc} */
    @Override
    public ScanResult scan(InputStream content, long contentLength) {
        scanCount.incrementAndGet();
        try {
            content.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (unavailable) {
            throw new VirusScanUnavailableException("scanner is unavailable (test stub)");
        }
        return result;
    }

    /**
     * 以降のスキャン結果を指定する。
     *
     * @param scanResult 返す結果
     */
    public void willReturn(ScanResult scanResult) {
        this.result = scanResult;
        this.unavailable = false;
    }

    /** 以降のスキャンで {@link VirusScanUnavailableException} を投げる。 */
    public void willBeUnavailable() {
        this.unavailable = true;
    }

    /**
     * スキャンが呼ばれた回数を返す。
     *
     * @return 呼び出し回数
     */
    public int scanCount() {
        return scanCount.get();
    }

    /** 既定（SAFE・稼働中・0 回）へ戻す。 */
    public void reset() {
        result = ScanResult.SAFE;
        unavailable = false;
        scanCount.set(0);
    }
}

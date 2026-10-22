package com.example.ddmsaa.infrastructure.scan;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 開発・テスト用の {@link VirusScanService}。スキャンせず常に SAFE を返し、
 * 本番で none を使っていないか検知できるよう warn ログを出す。
 */
@Component
@ConditionalOnProperty(name = "app.scan.type", havingValue = "none", matchIfMissing = true)
public class NoopVirusScanService implements VirusScanService {

    private static final Logger LOG = LoggerFactory.getLogger(NoopVirusScanService.class);

    /**
     * 生成する。
     */
    public NoopVirusScanService() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>内容は読み取らず、常に SAFE を返す。</p>
     */
    @Override
    public ScanResult scan(InputStream content, long contentLength) {
        LOG.warn("Virus scan is disabled (app.scan.type=none); treating content as SAFE");
        return ScanResult.SAFE;
    }
}

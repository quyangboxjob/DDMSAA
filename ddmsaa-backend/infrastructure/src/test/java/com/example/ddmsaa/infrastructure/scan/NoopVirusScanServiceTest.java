package com.example.ddmsaa.infrastructure.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NoopVirusScanService の開発用スキャン")
class NoopVirusScanServiceTest {

    @Test
    @DisplayName("内容によらず SAFE を返す")
    void alwaysSafe() {
        NoopVirusScanService service = new NoopVirusScanService();

        assertThat(service.scan(new ByteArrayInputStream(new byte[] {1, 2, 3}), 3))
                .isEqualTo(ScanResult.SAFE);
        assertThat(service.scan(null, 0)).isEqualTo(ScanResult.SAFE);
    }
}

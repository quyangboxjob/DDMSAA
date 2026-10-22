package com.example.ddmsaa.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@DisplayName("BusinessServiceCoreApplication のコンポーネントスキャン設定")
class BusinessServiceCoreApplicationTest {

    @Test
    @DisplayName("core・common・infrastructure の 3 パッケージをスキャンする")
    void scansThreePackages() {
        SpringBootApplication annotation =
                BusinessServiceCoreApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.scanBasePackages()).containsExactlyInAnyOrder(
                "com.example.ddmsaa.core", "com.example.ddmsaa.common",
                "com.example.ddmsaa.infrastructure");
    }
}

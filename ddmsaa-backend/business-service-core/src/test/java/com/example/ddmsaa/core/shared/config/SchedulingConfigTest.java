package com.example.ddmsaa.core.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@DisplayName("SchedulingConfig のスケジューリング有効化")
class SchedulingConfigTest {

    @Test
    @DisplayName("@Configuration と @EnableScheduling が付与されている")
    void hasAnnotations() {
        assertThat(SchedulingConfig.class.isAnnotationPresent(Configuration.class)).isTrue();
        assertThat(SchedulingConfig.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }
}

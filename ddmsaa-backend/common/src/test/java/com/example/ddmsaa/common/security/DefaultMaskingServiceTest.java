package com.example.ddmsaa.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultMaskingService の no-op 動作")
class DefaultMaskingServiceTest {

    private final DefaultMaskingService service = new DefaultMaskingService();

    @Test
    @DisplayName("target をそのまま（同一インスタンスで）返す")
    void returnsTargetAsIs() {
        Object target = new Object();

        assertThat(service.maskByRole(target, "CUSTOMER", false)).isSameAs(target);
    }

    @Test
    @DisplayName("target が null なら null を返す")
    void nullTarget() {
        Object result = service.maskByRole((Object) null, "ADMIN", true);

        assertThat(result).isNull();
    }
}

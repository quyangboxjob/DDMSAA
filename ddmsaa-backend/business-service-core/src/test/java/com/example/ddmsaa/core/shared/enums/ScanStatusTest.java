package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScanStatus（T07-10）のコード対応")
class ScanStatusTest {

    @Test
    @DisplayName("コードは PENDING=1, SAFE=2, REJECTED=3")
    void codes() {
        assertThat(ScanStatus.PENDING.getCode()).isEqualTo(1);
        assertThat(ScanStatus.SAFE.getCode()).isEqualTo(2);
        assertThat(ScanStatus.REJECTED.getCode()).isEqualTo(3);
        assertThat(ScanStatus.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(ScanStatus.fromCode(1)).isEqualTo(ScanStatus.PENDING);
        assertThat(ScanStatus.fromCode(2)).isEqualTo(ScanStatus.SAFE);
        assertThat(ScanStatus.fromCode(3)).isEqualTo(ScanStatus.REJECTED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(ScanStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> ScanStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ScanStatus.fromCode(4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

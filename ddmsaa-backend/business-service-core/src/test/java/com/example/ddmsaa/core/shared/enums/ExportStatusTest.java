package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExportStatus（T07-13）のコード対応")
class ExportStatusTest {

    @Test
    @DisplayName("コードは QUEUED=1, RUNNING=2, READY=3, FAILED=4, EXPIRED=5")
    void codes() {
        assertThat(ExportStatus.QUEUED.getCode()).isEqualTo(1);
        assertThat(ExportStatus.RUNNING.getCode()).isEqualTo(2);
        assertThat(ExportStatus.READY.getCode()).isEqualTo(3);
        assertThat(ExportStatus.FAILED.getCode()).isEqualTo(4);
        assertThat(ExportStatus.EXPIRED.getCode()).isEqualTo(5);
        assertThat(ExportStatus.values()).hasSize(5);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(ExportStatus.fromCode(1)).isEqualTo(ExportStatus.QUEUED);
        assertThat(ExportStatus.fromCode(2)).isEqualTo(ExportStatus.RUNNING);
        assertThat(ExportStatus.fromCode(3)).isEqualTo(ExportStatus.READY);
        assertThat(ExportStatus.fromCode(4)).isEqualTo(ExportStatus.FAILED);
        assertThat(ExportStatus.fromCode(5)).isEqualTo(ExportStatus.EXPIRED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(ExportStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> ExportStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ExportStatus.fromCode(6))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationStatus（T07-12）のコード対応")
class NotificationStatusTest {

    @Test
    @DisplayName("コードは QUEUED=1, SENDING=2, SENT=3, FAILED=4")
    void codes() {
        assertThat(NotificationStatus.QUEUED.getCode()).isEqualTo(1);
        assertThat(NotificationStatus.SENDING.getCode()).isEqualTo(2);
        assertThat(NotificationStatus.SENT.getCode()).isEqualTo(3);
        assertThat(NotificationStatus.FAILED.getCode()).isEqualTo(4);
        assertThat(NotificationStatus.values()).hasSize(4);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(NotificationStatus.fromCode(1)).isEqualTo(NotificationStatus.QUEUED);
        assertThat(NotificationStatus.fromCode(2)).isEqualTo(NotificationStatus.SENDING);
        assertThat(NotificationStatus.fromCode(3)).isEqualTo(NotificationStatus.SENT);
        assertThat(NotificationStatus.fromCode(4)).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(NotificationStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> NotificationStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificationStatus.fromCode(5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

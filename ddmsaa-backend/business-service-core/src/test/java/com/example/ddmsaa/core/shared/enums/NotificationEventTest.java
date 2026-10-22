package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationEvent（T07-11）のコード対応")
class NotificationEventTest {

    @Test
    @DisplayName("コードは ORDER_SUBMITTED=1 から EXPORT_COMPLETED=5 まで連番")
    void codes() {
        assertThat(NotificationEvent.ORDER_SUBMITTED.getCode()).isEqualTo(1);
        assertThat(NotificationEvent.ORDER_APPROVED.getCode()).isEqualTo(2);
        assertThat(NotificationEvent.ORDER_RETURNED.getCode()).isEqualTo(3);
        assertThat(NotificationEvent.ORDER_CANCELLED.getCode()).isEqualTo(4);
        assertThat(NotificationEvent.EXPORT_COMPLETED.getCode()).isEqualTo(5);
        assertThat(NotificationEvent.values()).hasSize(5);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(NotificationEvent.fromCode(1)).isEqualTo(NotificationEvent.ORDER_SUBMITTED);
        assertThat(NotificationEvent.fromCode(2)).isEqualTo(NotificationEvent.ORDER_APPROVED);
        assertThat(NotificationEvent.fromCode(3)).isEqualTo(NotificationEvent.ORDER_RETURNED);
        assertThat(NotificationEvent.fromCode(4)).isEqualTo(NotificationEvent.ORDER_CANCELLED);
        assertThat(NotificationEvent.fromCode(5)).isEqualTo(NotificationEvent.EXPORT_COMPLETED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(NotificationEvent.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> NotificationEvent.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificationEvent.fromCode(6))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

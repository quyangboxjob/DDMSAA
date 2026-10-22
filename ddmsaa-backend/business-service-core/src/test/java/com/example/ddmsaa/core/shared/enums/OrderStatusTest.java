package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderStatus（T07-6）のコード対応")
class OrderStatusTest {

    @Test
    @DisplayName("コードは DRAFT=1, PENDING_APPROVAL=2, RETURNED=3, APPROVED=4, CANCELLED=5")
    void codes() {
        assertThat(OrderStatus.DRAFT.getCode()).isEqualTo(1);
        assertThat(OrderStatus.PENDING_APPROVAL.getCode()).isEqualTo(2);
        assertThat(OrderStatus.RETURNED.getCode()).isEqualTo(3);
        assertThat(OrderStatus.APPROVED.getCode()).isEqualTo(4);
        assertThat(OrderStatus.CANCELLED.getCode()).isEqualTo(5);
        assertThat(OrderStatus.values()).hasSize(5);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(OrderStatus.fromCode(1)).isEqualTo(OrderStatus.DRAFT);
        assertThat(OrderStatus.fromCode(2)).isEqualTo(OrderStatus.PENDING_APPROVAL);
        assertThat(OrderStatus.fromCode(3)).isEqualTo(OrderStatus.RETURNED);
        assertThat(OrderStatus.fromCode(4)).isEqualTo(OrderStatus.APPROVED);
        assertThat(OrderStatus.fromCode(5)).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(OrderStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> OrderStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OrderStatus.fromCode(6))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

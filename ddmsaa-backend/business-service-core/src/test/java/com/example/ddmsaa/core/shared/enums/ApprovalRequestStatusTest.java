package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApprovalRequestStatus（T07-7）のコード対応")
class ApprovalRequestStatusTest {

    @Test
    @DisplayName("コードは PENDING=1, APPROVED=2, RETURNED=3")
    void codes() {
        assertThat(ApprovalRequestStatus.PENDING.getCode()).isEqualTo(1);
        assertThat(ApprovalRequestStatus.APPROVED.getCode()).isEqualTo(2);
        assertThat(ApprovalRequestStatus.RETURNED.getCode()).isEqualTo(3);
        assertThat(ApprovalRequestStatus.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(ApprovalRequestStatus.fromCode(1)).isEqualTo(ApprovalRequestStatus.PENDING);
        assertThat(ApprovalRequestStatus.fromCode(2)).isEqualTo(ApprovalRequestStatus.APPROVED);
        assertThat(ApprovalRequestStatus.fromCode(3)).isEqualTo(ApprovalRequestStatus.RETURNED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(ApprovalRequestStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> ApprovalRequestStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApprovalRequestStatus.fromCode(4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

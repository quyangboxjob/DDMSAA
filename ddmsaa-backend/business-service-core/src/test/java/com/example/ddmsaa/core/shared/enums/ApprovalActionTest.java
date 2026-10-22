package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApprovalAction（T07-8）のコード対応")
class ApprovalActionTest {

    @Test
    @DisplayName("コードは APPROVE=1, RETURN=2")
    void codes() {
        assertThat(ApprovalAction.APPROVE.getCode()).isEqualTo(1);
        assertThat(ApprovalAction.RETURN.getCode()).isEqualTo(2);
        assertThat(ApprovalAction.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(ApprovalAction.fromCode(1)).isEqualTo(ApprovalAction.APPROVE);
        assertThat(ApprovalAction.fromCode(2)).isEqualTo(ApprovalAction.RETURN);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(ApprovalAction.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> ApprovalAction.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ApprovalAction.fromCode(3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

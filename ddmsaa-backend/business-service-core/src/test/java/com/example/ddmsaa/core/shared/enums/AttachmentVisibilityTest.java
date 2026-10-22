package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AttachmentVisibility（T07-9）のコード対応")
class AttachmentVisibilityTest {

    @Test
    @DisplayName("コードは INTERNAL=1, CUSTOMER=2")
    void codes() {
        assertThat(AttachmentVisibility.INTERNAL.getCode()).isEqualTo(1);
        assertThat(AttachmentVisibility.CUSTOMER.getCode()).isEqualTo(2);
        assertThat(AttachmentVisibility.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(AttachmentVisibility.fromCode(1)).isEqualTo(AttachmentVisibility.INTERNAL);
        assertThat(AttachmentVisibility.fromCode(2)).isEqualTo(AttachmentVisibility.CUSTOMER);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(AttachmentVisibility.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> AttachmentVisibility.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AttachmentVisibility.fromCode(3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

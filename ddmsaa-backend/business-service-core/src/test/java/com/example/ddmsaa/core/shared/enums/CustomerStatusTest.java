package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerStatus（T07-3）のコード対応")
class CustomerStatusTest {

    @Test
    @DisplayName("コードは ACTIVE=1, INACTIVE=2")
    void codes() {
        assertThat(CustomerStatus.ACTIVE.getCode()).isEqualTo(1);
        assertThat(CustomerStatus.INACTIVE.getCode()).isEqualTo(2);
        assertThat(CustomerStatus.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(CustomerStatus.fromCode(1)).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(CustomerStatus.fromCode(2)).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(CustomerStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> CustomerStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CustomerStatus.fromCode(3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

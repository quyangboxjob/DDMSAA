package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserStatus（T07-2）のコード対応")
class UserStatusTest {

    @Test
    @DisplayName("コードは ACTIVE=1, DISABLED=2")
    void codes() {
        assertThat(UserStatus.ACTIVE.getCode()).isEqualTo(1);
        assertThat(UserStatus.DISABLED.getCode()).isEqualTo(2);
        assertThat(UserStatus.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(UserStatus.fromCode(1)).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.fromCode(2)).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(UserStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> UserStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UserStatus.fromCode(3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

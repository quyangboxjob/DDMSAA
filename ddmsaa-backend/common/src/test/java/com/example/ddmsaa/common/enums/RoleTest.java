package com.example.ddmsaa.common.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Role（T07-1）のコード対応")
class RoleTest {

    @Test
    @DisplayName("コードは ADMIN=1・EMPLOYEE=2・CUSTOMER=3")
    void codes() {
        assertThat(Role.ADMIN.getCode()).isEqualTo(1);
        assertThat(Role.EMPLOYEE.getCode()).isEqualTo(2);
        assertThat(Role.CUSTOMER.getCode()).isEqualTo(3);
        assertThat(Role.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(Role.fromCode(1)).isEqualTo(Role.ADMIN);
        assertThat(Role.fromCode(2)).isEqualTo(Role.EMPLOYEE);
        assertThat(Role.fromCode(3)).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(Role.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> Role.fromCode(99)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Role.fromCode(0)).isInstanceOf(IllegalArgumentException.class);
    }
}

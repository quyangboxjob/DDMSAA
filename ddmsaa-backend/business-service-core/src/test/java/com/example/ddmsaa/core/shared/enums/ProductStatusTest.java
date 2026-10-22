package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductStatus（T07-4）のコード対応")
class ProductStatusTest {

    @Test
    @DisplayName("コードは ACTIVE=1, INACTIVE=2")
    void codes() {
        assertThat(ProductStatus.ACTIVE.getCode()).isEqualTo(1);
        assertThat(ProductStatus.INACTIVE.getCode()).isEqualTo(2);
        assertThat(ProductStatus.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(ProductStatus.fromCode(1)).isEqualTo(ProductStatus.ACTIVE);
        assertThat(ProductStatus.fromCode(2)).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(ProductStatus.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> ProductStatus.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProductStatus.fromCode(3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

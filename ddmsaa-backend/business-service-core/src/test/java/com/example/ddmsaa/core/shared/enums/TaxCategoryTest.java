package com.example.ddmsaa.core.shared.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TaxCategory（T07-5）のコード対応")
class TaxCategoryTest {

    @Test
    @DisplayName("コードは STANDARD=1, REDUCED=2, EXEMPT=3")
    void codes() {
        assertThat(TaxCategory.STANDARD.getCode()).isEqualTo(1);
        assertThat(TaxCategory.REDUCED.getCode()).isEqualTo(2);
        assertThat(TaxCategory.EXEMPT.getCode()).isEqualTo(3);
        assertThat(TaxCategory.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromCode は対応する enum を返す")
    void fromCode() {
        assertThat(TaxCategory.fromCode(1)).isEqualTo(TaxCategory.STANDARD);
        assertThat(TaxCategory.fromCode(2)).isEqualTo(TaxCategory.REDUCED);
        assertThat(TaxCategory.fromCode(3)).isEqualTo(TaxCategory.EXEMPT);
    }

    @Test
    @DisplayName("fromCode は null なら null を返す")
    void fromCodeNull() {
        assertThat(TaxCategory.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode は未知のコードで IllegalArgumentException")
    void fromCodeUnknown() {
        assertThatThrownBy(() -> TaxCategory.fromCode(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TaxCategory.fromCode(4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

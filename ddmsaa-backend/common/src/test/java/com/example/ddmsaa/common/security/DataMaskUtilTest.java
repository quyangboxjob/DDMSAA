package com.example.ddmsaa.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataMaskUtil のマスク処理")
class DataMaskUtilTest {

    @Test
    @DisplayName("maskKeepLast は先頭を * に置換し末尾 last 文字を残す")
    void keepLast() {
        assertThat(DataMaskUtil.maskKeepLast("1234567890", 4)).isEqualTo("******7890");
    }

    @Test
    @DisplayName("maskKeepLast は last が 0 なら全て * にする")
    void keepLastZero() {
        assertThat(DataMaskUtil.maskKeepLast("abc", 0)).isEqualTo("***");
    }

    @Test
    @DisplayName("maskKeepLast は last が長さ以上なら全表示")
    void keepLastAtLeastLength() {
        assertThat(DataMaskUtil.maskKeepLast("abc", 3)).isEqualTo("abc");
        assertThat(DataMaskUtil.maskKeepLast("abc", 10)).isEqualTo("abc");
    }

    @Test
    @DisplayName("maskKeepLast は null・空・空白をそのまま返す")
    void keepLastBlank() {
        assertThat(DataMaskUtil.maskKeepLast(null, 4)).isNull();
        assertThat(DataMaskUtil.maskKeepLast("", 4)).isEmpty();
        assertThat(DataMaskUtil.maskKeepLast("  ", 4)).isEqualTo("  ");
    }

    @Test
    @DisplayName("maskKeepLast は last が負なら IllegalArgumentException")
    void keepLastNegative() {
        assertThatThrownBy(() -> DataMaskUtil.maskKeepLast("abc", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("maskAll は全文字を * にする")
    void maskAll() {
        assertThat(DataMaskUtil.maskAll("secret")).isEqualTo("******");
    }

    @Test
    @DisplayName("maskAll は null・空・空白をそのまま返す")
    void maskAllBlank() {
        assertThat(DataMaskUtil.maskAll(null)).isNull();
        assertThat(DataMaskUtil.maskAll("")).isEmpty();
        assertThat(DataMaskUtil.maskAll(" ")).isEqualTo(" ");
    }
}

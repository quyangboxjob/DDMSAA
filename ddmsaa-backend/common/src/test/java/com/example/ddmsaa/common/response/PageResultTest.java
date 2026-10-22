package com.example.ddmsaa.common.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PageResult.of の境界条件")
class PageResultTest {

    @Test
    @DisplayName("pages は ceil(total / pageSize) で計算される")
    void pagesIsCeil() {
        PageResult<String> result = PageResult.of(List.of("a"), 21, 1, 10);

        assertThat(result.pages()).isEqualTo(3);
        assertThat(result.total()).isEqualTo(21);
        assertThat(result.pageNum()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.records()).containsExactly("a");
    }

    @Test
    @DisplayName("total がちょうど割り切れる場合の pages")
    void pagesExact() {
        assertThat(PageResult.of(List.of(), 20, 1, 10).pages()).isEqualTo(2);
    }

    @Test
    @DisplayName("total が 0 なら pages は 0")
    void zeroTotal() {
        assertThat(PageResult.of(List.of(), 0, 1, 10).pages()).isZero();
    }

    @Test
    @DisplayName("pageNum が pages を超えても空 records で返す")
    void pageNumBeyondPages() {
        PageResult<String> result = PageResult.of(List.of(), 5, 9, 10);

        assertThat(result.records()).isEmpty();
        assertThat(result.pages()).isEqualTo(1);
    }

    @Test
    @DisplayName("records が null なら空リストとして扱う")
    void nullRecords() {
        assertThat(PageResult.<String>of(null, 0, 1, 10).records()).isEmpty();
    }

    @Test
    @DisplayName("pageSize が 0 以下なら IllegalArgumentException")
    void invalidPageSize() {
        assertThatThrownBy(() -> PageResult.of(List.of(), 1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResult.of(List.of(), 1, 1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("total が負数なら IllegalArgumentException")
    void negativeTotal() {
        assertThatThrownBy(() -> PageResult.of(List.of(), -1, 1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

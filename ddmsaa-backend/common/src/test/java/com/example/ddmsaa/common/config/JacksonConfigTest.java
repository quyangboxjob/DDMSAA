package com.example.ddmsaa.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JacksonConfig: API 日時の出力形式")
class JacksonConfigTest {

    private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules()
            .addModule(new JacksonConfig().ddmsaaDateTimeModule()).build();

    private String json(OffsetDateTime value) throws Exception {
        return mapper.writeValueAsString(value);
    }

    @Test
    @DisplayName("UTC の値を日本時間（+09:00）に変換して出力する")
    void utcIsConvertedToTokyo() throws Exception {
        OffsetDateTime utc = OffsetDateTime.of(2026, 10, 21, 16, 5, 30, 123_456_000,
                ZoneOffset.UTC);

        assertThat(json(utc)).isEqualTo("\"2026-10-22T01:05:30.123456+09:00\"");
    }

    @Test
    @DisplayName("秒未満はマイクロ秒 6 桁に固定する（末尾の 0 を省略しない）")
    void fractionIsFixedToSixDigits() throws Exception {
        OffsetDateTime exact = OffsetDateTime.of(2026, 10, 22, 10, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime millis = exact.withNano(120_000_000);

        assertThat(json(exact)).isEqualTo("\"2026-10-22T10:00:00.000000+09:00\"");
        assertThat(json(millis)).isEqualTo("\"2026-10-22T10:00:00.120000+09:00\"");
    }

    @Test
    @DisplayName("ナノ秒はマイクロ秒へ切り捨てる")
    void nanosAreTruncatedToMicros() throws Exception {
        OffsetDateTime nanos = OffsetDateTime.of(2026, 10, 22, 10, 0, 0, 123_456_789,
                ZoneOffset.ofHours(9));

        assertThat(json(nanos)).isEqualTo("\"2026-10-22T10:00:00.123456+09:00\"");
    }

    @Test
    @DisplayName("出力した文字列を読み戻すと同じ時刻になる（楽観ロックの往復）")
    void roundTripKeepsInstant() throws Exception {
        OffsetDateTime original = OffsetDateTime.of(2026, 10, 21, 23, 59, 59, 999_999_000,
                ZoneOffset.UTC);

        OffsetDateTime back = mapper.readValue(json(original), OffsetDateTime.class);

        assertThat(back.toInstant()).isEqualTo(original.toInstant());
    }
}

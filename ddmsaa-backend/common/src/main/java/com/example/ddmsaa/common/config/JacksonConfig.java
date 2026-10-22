package com.example.ddmsaa.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API の日時の出力形式を共通化する。{@link OffsetDateTime} は日本時間（{@code +09:00}）の
 * ISO 8601 で、秒未満をマイクロ秒 6 桁固定にする
 * （{@code yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX}。spec-12 §6、楽観ロックの {@code updatedAt}）。
 *
 * <p>PostgreSQL の JDBC は timestamptz を UTC の OffsetDateTime で返すため、この設定がないと
 * {@code +00:00} と桁数の揃わない値が API に出る。</p>
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    /** 表示・API のタイムゾーン（spec-11：Asia/Tokyo）。 */
    public static final ZoneId API_ZONE = ZoneId.of("Asia/Tokyo");

    /** API の日時書式。 */
    public static final DateTimeFormatter API_DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX").withZone(API_ZONE);

    /**
     * {@link OffsetDateTime} のシリアライザを差し替える Jackson モジュール。
     * Spring Boot が ObjectMapper に自動登録する。
     *
     * @return モジュール
     */
    @Bean
    public Module ddmsaaDateTimeModule() {
        SimpleModule module = new SimpleModule("ddmsaa-date-time");
        module.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        return module;
    }

    /** 日本時間・マイクロ秒 6 桁で出力するシリアライザ。 */
    static final class OffsetDateTimeSerializer extends StdSerializer<OffsetDateTime> {

        private static final long serialVersionUID = 1L;

        OffsetDateTimeSerializer() {
            super(OffsetDateTime.class);
        }

        /** {@inheritDoc} */
        @Override
        public void serialize(OffsetDateTime value, JsonGenerator generator,
                SerializerProvider provider) throws IOException {
            generator.writeString(API_DATE_TIME.format(value));
        }
    }
}

package com.example.ddmsaa.auth;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DDMSAA の認証サービス（Magic Link 認証・JWT 発行）の起動クラス。
 *
 * <p>共有モジュール（common / infrastructure）の Bean を取り込むため、3 パッケージを
 * {@code scanBasePackages} に明示する。Mapper は {@code @Mapper} の自動検出に任せ、
 * {@code @MapperScan} は付与しない。</p>
 */
@SpringBootApplication(scanBasePackages = {
        "com.example.ddmsaa.auth",
        "com.example.ddmsaa.common",
        "com.example.ddmsaa.infrastructure"
})
public class AuthApplication {

    /**
     * 認証サービスを起動する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        // システムタイムゾーンは Asia/Tokyo（spec-11）。JVM の既定が UTC の環境でも、
        // 日付の既定値や JDBC のセッション時刻が日本時間になるようにする。
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        SpringApplication.run(AuthApplication.class, args);
    }
}

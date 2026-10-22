package com.example.ddmsaa.auth;

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
        SpringApplication.run(AuthApplication.class, args);
    }
}

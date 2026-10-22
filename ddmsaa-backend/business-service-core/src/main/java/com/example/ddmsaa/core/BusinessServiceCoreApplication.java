package com.example.ddmsaa.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * business-service-core の起動クラス。
 *
 * <p>common / infrastructure の Bean を取り込むため 3 パッケージを scanBasePackages に明示する。
 * {@code @MapperScan} は付与しない（{@code @Mapper} を自動検出する）。</p>
 */
@SpringBootApplication(scanBasePackages = {"com.example.ddmsaa.core",
        "com.example.ddmsaa.common", "com.example.ddmsaa.infrastructure"})
public class BusinessServiceCoreApplication {

    /**
     * アプリケーションを起動する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceCoreApplication.class, args);
    }
}

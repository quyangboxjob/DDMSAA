package com.example.ddmsaa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

/**
 * DDMSAA の API ゲートウェイ（Spring Cloud Gateway / WebFlux）の起動クラス。
 *
 * <p>境界制御（JWT 検証・ルーティング・CORS・エラー整形・traceId 貫通）のみを担い、
 * 業務ロジックは持たない。
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * ゲートウェイを起動する。
     *
     * <p>{@code TraceIdGlobalFilter} の Reactor Context による MDC 伝播の前提として、
     * {@link SpringApplication#run} の前に自動コンテキスト伝播を有効化する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayApplication.class, args);
    }
}

package com.example.ddmsaa.core.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * スケジューリングの有効化設定。
 *
 * <p>バッチ（BAT-01 通知送信、BAT-03 出力期限切れ、BAT-04 添付保持期限、BAT-05 ウイルススキャン）の
 * {@code @Scheduled} を有効にするため、{@code @EnableScheduling} をここで一度だけ宣言する。
 * 各 feature では重ねて宣言しない。</p>
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

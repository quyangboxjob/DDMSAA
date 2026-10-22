package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.core.shared.enums.NotificationEvent;

/**
 * 通知ポート。宛先解決（BR-NTF-003）と {@code notification_logs} の INSERT を一箇所に集約する。
 * メール送信は行わない（BAT-01）。
 *
 * <p>所有者（実装）：{@code approval}（{@code NotificationPortImpl}。通知の所有者）。
 * 主な呼び出し元：{@code order}（申請・取消し）、{@code approval}（承認・差戻し）、
 * {@code report}（BAT-02 完了）。</p>
 *
 * <p>トランザクション：書込を伴い、呼び出し側のトランザクションに参加する
 * （実装は {@code @Transactional(propagation = MANDATORY)}）。</p>
 */
public interface NotificationPort {

    /**
     * 通知を投入する。
     *
     * <p>BR-NTF-003 に従い宛先を解決し（同一メールアドレスは 1 件）、{@code notification_logs} に
     * {@code QUEUED}（{@code attempt_count = 0}、{@code next_attempt_at} は現在）を宛先ごとに
     * INSERT する。宛先 0 件は何も作らない。宛先のメールアドレスが解決できない場合は
     * 例外を投げず、その宛先を挿入せず warn ログを出す。</p>
     *
     * @param event 通知イベント
     * @param subject 通知の対象
     */
    void enqueue(NotificationEvent event, NotificationSubject subject);
}

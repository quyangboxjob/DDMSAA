package com.example.ddmsaa.core.shared.port;

import java.util.Map;

/**
 * 監査ログポート。{@code audit_logs} は共通の記録部品であり、各 feature が {@code user} の Service を
 * 直接参照しないための窓口。
 *
 * <p>所有者（実装）：{@code user}（{@code AuditLogServiceImpl}）。呼び出し元：全 feature
 * （{@link #recordDenied} は {@code OrderAccessPort} など範囲判定処理と、
 * core の {@code PermissionDeniedAuditListener}）。</p>
 *
 * <p>トランザクション：{@link #record} は呼び出し側のトランザクションに参加する
 * （実装は {@code @Transactional(propagation = MANDATORY)}）。{@link #recordDenied} は
 * {@code REQUIRES_NEW}（新しいトランザクション）で保存する。</p>
 */
public interface AuditLogPort {

    /**
     * 監査ログを記録する。
     *
     * <p>{@code actor_user_id} は {@code UserContext.getUserId()}（なければ NULL・
     * {@code created_by = SYSTEM}）。</p>
     *
     * @param auditAction 操作名（BR-AUD-001）
     * @param targetType 対象の種別
     * @param targetId 対象の ID
     * @param detail 任意の補足（null 可）
     */
    void record(String auditAction, String targetType, String targetId,
            Map<String, Object> detail);

    /**
     * 権限拒否（{@code PERMISSION_DENIED}）を記録する。
     *
     * <p>新しいトランザクション（{@code REQUIRES_NEW}）で保存するため、呼び出し側が例外・
     * ロールバックしても残る。403 は {@code PermissionDeniedAuditListener} が、範囲外の単件 404 は
     * 範囲判定処理が {@code EntityNotFoundException("E0005")} を投げる前に呼ぶ。
     * 一覧の自動除外では記録しない。</p>
     *
     * @param targetType 対象の種別
     * @param targetId 対象の ID
     */
    void recordDenied(String targetType, String targetId);
}

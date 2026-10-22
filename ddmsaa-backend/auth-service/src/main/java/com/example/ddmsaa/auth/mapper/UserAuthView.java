package com.example.ddmsaa.auth.mapper;

import java.time.OffsetDateTime;

/**
 * 認証判定に用いるユーザービュー（users / user_contacts / system_locks の LEFT JOIN 結果）。
 *
 * @param userId ユーザー ID
 * @param role ロールの INT コード（T07-1）
 * @param disabled 停用中（users.status = DISABLED）なら true
 * @param lockStartedAt ロック開始日時。ロックなしは null
 * @param email Magic Link の送信先メールアドレス。連絡先なしは null
 */
public record UserAuthView(
        String userId,
        Integer role,
        boolean disabled,
        OffsetDateTime lockStartedAt,
        String email) {
}

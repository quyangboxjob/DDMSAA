package com.example.ddmsaa.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ログイン監査を {@code audit_logs} へ追記するマッパー（INSERT のみ）。
 *
 * <p>auth-service の {@code audit_logs} 権限は INSERT のみのため、検索・更新・削除は
 * 定義しない。SQL は {@code mapper/AuthAuditMapper.xml} に定義する。</p>
 */
@Mapper
public interface AuthAuditMapper {

    /**
     * 監査ログを 1 件追記する。id は DB の採番関数（{@code next_business_id}）、
     * {@code created_by} は既定値 {@code SYSTEM} を用いる。
     *
     * @param actorUserId 操作者のユーザー ID。未認証の操作は null
     * @param auditAction 操作（{@code LOGIN_REQUESTED} / {@code LOGIN_SUCCEEDED} /
     *            {@code LOGIN_FAILED}）
     * @param targetType 対象種別（{@code USER}）
     * @param targetId 対象 ID（最大 10 文字）。無い場合は null
     * @param traceId トレース ID。無い場合は null
     * @return 挿入件数
     */
    int insert(@Param("actorUserId") String actorUserId,
            @Param("auditAction") String auditAction,
            @Param("targetType") String targetType,
            @Param("targetId") String targetId,
            @Param("traceId") String traceId);
}

package com.example.ddmsaa.common.security;

/**
 * 閲覧者のロールに応じて応答オブジェクトの項目をマスクするサービス。
 */
public interface MaskingService {

    /**
     * 閲覧者のロールと本人判定に応じて対象をマスクする。
     *
     * @param target マスク対象
     * @param viewerRole 閲覧者のロール
     * @param isSelf 閲覧者が対象データの本人なら true
     * @param <T> 対象の型
     * @return マスク後の対象
     */
    <T> T maskByRole(T target, String viewerRole, boolean isSelf);
}

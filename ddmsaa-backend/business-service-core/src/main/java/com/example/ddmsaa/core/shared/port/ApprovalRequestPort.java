package com.example.ddmsaa.core.shared.port;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 承認依頼ポート。承認ルールの選定・{@code rule_snapshot}・{@code approval_requests} は
 * {@code approval} のデータであり、呼び出し側が {@code ApprovalRequestMapper} を直接触らないための窓口。
 *
 * <p>所有者（実装）：{@code approval}（{@code ApprovalRequestPortImpl}）。
 * 主な呼び出し元：{@code order}（{@code submitOrder} が {@link #openRequest}、
 * {@code getOrder} が {@link #findPending}）。</p>
 *
 * <p>トランザクション：{@link #openRequest} は書込を伴い、呼び出し側のトランザクションに参加する
 * （実装は {@code @Transactional(propagation = MANDATORY)}。TX がなければ例外）。
 * {@link #findPending} は読取専用。例外は握りつぶさず呼び出し側 API の応答になる。</p>
 */
public interface ApprovalRequestPort {

    /**
     * 承認依頼を作成する。
     *
     * <p>申請日（日本時間）時点で有効な承認ルールを {@code totalAmount} の金額帯で選び、
     * {@code rule_snapshot} を保存して {@code approval_requests} を PENDING で INSERT する。
     * 再申請でも毎回新しい行を作る。</p>
     *
     * @param orderId 注文番号
     * @param submittedBy 申請者のユーザー ID（{@code approval_requests.created_by} に明示する。
     *        {@code UserContext.getUserId()} と同じ値）
     * @param totalAmount 再計算後の合計金額
     * @throws com.example.ddmsaa.common.exception.BusinessException 該当ルールなし
     *         （{@code E0006}、409）の場合。呼び出し側全体がロールバックされる
     */
    void openRequest(String orderId, String submittedBy, BigDecimal totalAmount);

    /**
     * 承認待ちの承認依頼を取得する（読取専用）。
     *
     * @param orderId 注文番号
     * @return PENDING の承認依頼（最新 1 件）。なければ {@link Optional#empty()}
     */
    Optional<PendingApproval> findPending(String orderId);
}

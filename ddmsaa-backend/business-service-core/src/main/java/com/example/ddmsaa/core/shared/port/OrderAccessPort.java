package com.example.ddmsaa.core.shared.port;

/**
 * 注文アクセスポート。「注文が呼び出し者の範囲で見えるか」の判定と注文行ロックを {@code order} の
 * 一箇所に集約し、他 feature が {@code OrderMapper} を触らないための窓口。
 *
 * <p>所有者（実装）：{@code order}（{@code OrderAccessPortImpl}）。
 * 主な呼び出し元：{@code approval}（承認履歴）、{@code attachment}（一覧・登録・取得・削除）、
 * {@code report}（PDF）。</p>
 *
 * <p>トランザクション：{@link #requireVisible} は読取専用。{@link #lockForUpdate} は呼び出し側の
 * トランザクションに参加する（TX 必須）。いずれも、注文が存在するが範囲外のときは
 * {@code AuditLogPort.recordDenied("ORDER", orderId)} を呼んでから
 * {@code EntityNotFoundException("E0005")} を投げ、不存在のときは記録せずに同じ例外を投げる
 * （応答は同じで存在を開示しない。BR-AUTH-006）。</p>
 */
public interface OrderAccessPort {

    /**
     * 呼び出し者の範囲で注文が見えることを確認する（ロック・更新はしない）。
     *
     * @param orderId 注文番号
     * @return 注文の可視情報
     * @throws com.example.ddmsaa.common.exception.EntityNotFoundException 注文が存在しない、
     *         または範囲外（{@code E0005}）の場合
     */
    OrderAccess requireVisible(String orderId);

    /**
     * {@link #requireVisible} と同じ範囲判定に加え、注文行を {@code SELECT ... FOR UPDATE} で
     * ロックする（添付登録など、注文に紐づく書込の直列化用）。
     *
     * @param orderId 注文番号
     * @return 注文の可視情報
     * @throws com.example.ddmsaa.common.exception.EntityNotFoundException 注文が存在しない、
     *         または範囲外（{@code E0005}）の場合
     */
    OrderAccess lockForUpdate(String orderId);
}

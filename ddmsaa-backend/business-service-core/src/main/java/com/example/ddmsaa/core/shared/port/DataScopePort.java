package com.example.ddmsaa.core.shared.port;

/**
 * データ範囲ポート。データ範囲（BR-AUTH-003）の求め方を一箇所に集約する。
 *
 * <p>所有者（実装）：{@code customer}（担当割当 {@code customer_assignments} の所有者。
 * {@code DataScopePortImpl}）。呼び出し元：{@code order}、{@code report}、{@code attachment}、
 * {@code customer}、{@code user}（顧客選択肢）。</p>
 *
 * <p>トランザクション：読取専用。</p>
 */
public interface DataScopePort {

    /**
     * {@code UserContext} から呼び出し者のデータ範囲を求める。
     *
     * <p>EMPLOYEE の担当顧客が 0 件なら {@code customerIds} は空集合
     * （Mapper 側は「該当なし」として扱う）。</p>
     *
     * @return 呼び出し者のデータ範囲
     */
    DataScope currentScope();
}

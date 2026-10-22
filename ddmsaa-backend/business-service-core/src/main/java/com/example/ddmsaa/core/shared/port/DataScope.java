package com.example.ddmsaa.core.shared.port;

import com.example.ddmsaa.common.enums.Role;
import java.util.Set;

/**
 * 呼び出し者のデータ範囲（BR-AUTH-003）。{@link DataScopePort#currentScope()} の戻り値。
 *
 * <p>ADMIN は {@link #isAll()}（全件）。EMPLOYEE は有効な担当割当
 * （{@code customer_assignments.is_active = TRUE} かつ {@code deleted_at IS NULL}）の顧客 ID の集合、
 * CUSTOMER は自社 {@code customer_id} の 1 件。Mapper の条件（{@code cond}）に使う。
 * {@code customerIds} が null のときは空集合として扱い、以後は変更できない集合になる。</p>
 *
 * @param role 呼び出し者のロール
 * @param userId 呼び出し者のユーザー ID
 * @param customerIds 参照できる顧客 ID の集合（空集合は「該当なし」）
 */
public record DataScope(Role role, String userId, Set<String> customerIds) {

    /**
     * 顧客 ID の集合を変更不可の集合に正規化する。
     *
     * @param role 呼び出し者のロール
     * @param userId 呼び出し者のユーザー ID
     * @param customerIds 参照できる顧客 ID の集合（null 可。null は空集合）
     */
    public DataScope {
        customerIds = customerIds == null ? Set.of() : Set.copyOf(customerIds);
    }

    /**
     * 全件を参照できる範囲かどうかを返す。
     *
     * @return ロールが ADMIN の場合 true
     */
    public boolean isAll() {
        return role == Role.ADMIN;
    }
}

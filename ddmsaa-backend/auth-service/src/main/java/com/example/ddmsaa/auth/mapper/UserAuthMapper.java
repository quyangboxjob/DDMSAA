package com.example.ddmsaa.auth.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * users / user_contacts / system_locks の LEFT JOIN 結果を READ ONLY で取得するマッパー。
 *
 * <p>SQL は {@code mapper/UserAuthMapper.xml} に定義する。</p>
 */
@Mapper
public interface UserAuthMapper {

    /**
     * 認証判定用のユーザービューを取得する。
     *
     * @param userId ユーザー ID
     * @return ユーザービュー。存在しない・論理削除済みの場合は null
     */
    UserAuthView findAuthView(String userId);
}

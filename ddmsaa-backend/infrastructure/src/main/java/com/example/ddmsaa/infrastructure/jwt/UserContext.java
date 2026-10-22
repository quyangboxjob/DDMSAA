package com.example.ddmsaa.infrastructure.jwt;

/**
 * ログイン者のユーザー ID とロールをスレッド単位で保持するホルダー。
 *
 * <p>{@link #set(String, String)} を呼び出したスレッドは、スレッドプールでの再利用による汚染を
 * 防ぐため、必ず finally ブロックで {@link #clear()} を呼び出すこと。
 * {@code JwtAuthenticationFilter} がリクエストごとに設定とクリアを行う。</p>
 */
public final class UserContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 現在のスレッドにログイン者情報を設定する。
     *
     * @param userId ユーザー ID
     * @param role ロール（ADMIN / EMPLOYEE / CUSTOMER）
     */
    public static void set(String userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    /**
     * 現在のスレッドのユーザー ID を返す。
     *
     * @return ユーザー ID。未設定（非 HTTP スレッド等）の場合は null
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * 現在のスレッドのロールを返す。
     *
     * @return ロール。未設定の場合は null
     */
    public static String getRole() {
        return ROLE.get();
    }

    /**
     * 現在のスレッドのログイン者情報を消去する。未設定でも例外にならない（冪等）。
     */
    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}

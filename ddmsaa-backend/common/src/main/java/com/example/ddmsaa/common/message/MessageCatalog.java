package com.example.ddmsaa.common.message;

/**
 * メッセージ ID から日本語正本の文言を解決するカタログ。
 */
public interface MessageCatalog {

    /**
     * メッセージ ID の文言を解決し、{@code {0}} 形式のプレースホルダを引数で置換する。
     *
     * @param messageId メッセージ ID
     * @param args 置換引数（null 可）
     * @return 解決した文言。未登録の ID は messageId 自体、messageId が null なら null
     */
    String resolve(String messageId, Object... args);

    /**
     * メッセージ ID が登録済みか判定する。
     *
     * @param messageId メッセージ ID（null 可）
     * @return 登録済みなら true
     */
    boolean exists(String messageId);
}

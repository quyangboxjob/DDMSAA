package com.example.ddmsaa.infrastructure.rest;

/**
 * サービス間 HTTP 通信の抽象クライアント。GET / POST / PUT / DELETE を提供する。
 *
 * <p>base-url の解決、ユーザー識別ヘッダーと X-Request-Id の透過、タイムアウト設定は
 * {@link RestClientFactory} が適用する。</p>
 */
public interface ServiceClient {

    /**
     * GET を実行する。
     *
     * @param path base-url からの相対パス
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスの型
     * @return デシリアライズ済みのレスポンスボディ
     * @throws com.example.ddmsaa.common.exception.BusinessException 接続タイムアウト（E0013）、
     *         読み取りタイムアウト（E0043）、ダウンストリームの 4xx の場合
     */
    <T> T get(String path, Class<T> responseType);

    /**
     * POST を実行する。
     *
     * @param path base-url からの相対パス
     * @param body リクエストボディ（null 可）
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスの型
     * @return デシリアライズ済みのレスポンスボディ
     * @throws com.example.ddmsaa.common.exception.BusinessException 接続タイムアウト（E0013）、
     *         読み取りタイムアウト（E0043）、ダウンストリームの 4xx の場合
     */
    <T> T post(String path, Object body, Class<T> responseType);

    /**
     * PUT を実行する。
     *
     * @param path base-url からの相対パス
     * @param body リクエストボディ（null 可）
     * @param responseType レスポンスボディの型
     * @param <T> レスポンスの型
     * @return デシリアライズ済みのレスポンスボディ
     * @throws com.example.ddmsaa.common.exception.BusinessException 接続タイムアウト（E0013）、
     *         読み取りタイムアウト（E0043）、ダウンストリームの 4xx の場合
     */
    <T> T put(String path, Object body, Class<T> responseType);

    /**
     * DELETE を実行する。
     *
     * @param path base-url からの相対パス
     * @throws com.example.ddmsaa.common.exception.BusinessException 接続タイムアウト（E0013）、
     *         読み取りタイムアウト（E0043）、ダウンストリームの 4xx の場合
     */
    void delete(String path);
}

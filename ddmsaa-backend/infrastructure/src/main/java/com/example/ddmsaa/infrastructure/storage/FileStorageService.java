package com.example.ddmsaa.infrastructure.storage;

import java.io.InputStream;

/**
 * 外部オブジェクトストレージへのアップロード・ダウンロード・削除・署名付き URL 生成を
 * 抽象化するインタフェース。
 *
 * <p>本層は保存・取得・削除・署名 URL 生成のみを担う。content type の許可やサイズ上限などの
 * 業務ポリシー検証は業務層で行う。</p>
 */
public interface FileStorageService {

    /**
     * オブジェクトを保存する。同じキーが既にあれば上書きする。
     *
     * @param objectKey オブジェクトキー
     * @param content 保存する内容（呼び出し側が close する）
     * @param contentLength 内容のバイト数
     * @param contentType コンテンツタイプ
     * @throws IllegalArgumentException objectKey または content が null の場合
     */
    void upload(String objectKey, InputStream content, long contentLength, String contentType);

    /**
     * オブジェクトを取得する。
     *
     * @param objectKey オブジェクトキー
     * @return 内容のストリーム（呼び出し側が close する）
     * @throws com.example.ddmsaa.common.exception.EntityNotFoundException
     *         オブジェクトが存在しない場合（E0005）
     */
    InputStream download(String objectKey);

    /**
     * オブジェクトを削除する。存在しない場合も正常終了する（冪等）。
     *
     * @param objectKey オブジェクトキー
     */
    void delete(String objectKey);

    /**
     * ダウンロード用の署名付き URL を生成する。実体の存在は確認しない。
     *
     * @param objectKey オブジェクトキー
     * @param expirySeconds 有効期間（秒）
     * @return URL 文字列
     * @throws IllegalArgumentException objectKey が null の場合
     */
    String presignedUrl(String objectKey, int expirySeconds);
}

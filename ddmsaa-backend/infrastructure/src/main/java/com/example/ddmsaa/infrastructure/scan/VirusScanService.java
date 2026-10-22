package com.example.ddmsaa.infrastructure.scan;

import java.io.InputStream;

/**
 * 添付ファイルのウイルススキャンを抽象化するインタフェース。
 * 結果は SAFE / REJECTED / 実施不能（例外）の 3 通り。
 */
public interface VirusScanService {

    /**
     * 内容をスキャンする。
     *
     * @param content スキャン対象の内容（呼び出し側が close する）
     * @param contentLength 内容のバイト数
     * @return {@link ScanResult#SAFE} または {@link ScanResult#REJECTED}
     * @throws VirusScanUnavailableException スキャナ不達・タイムアウト等で実施できない場合
     */
    ScanResult scan(InputStream content, long contentLength);
}

package com.example.ddmsaa.infrastructure.scan;

/**
 * ウイルススキャンの結果。スキャン自体が実施できなかった場合は結果ではなく
 * {@link VirusScanUnavailableException} で表す。
 */
public enum ScanResult {

    /** 脅威は検出されなかった。 */
    SAFE,

    /** 脅威が検出された。 */
    REJECTED
}

package com.example.ddmsaa.core.shared.enums;

/**
 * 出力状態（T07-13。DB export_status_codes の INT コードに対応）。
 */
public enum ExportStatus {

    /** 待機中。 */
    QUEUED(1),

    /** 生成中。 */
    RUNNING(2),

    /** ダウンロード可。 */
    READY(3),

    /** 失敗。 */
    FAILED(4),

    /** 期限切れ。 */
    EXPIRED(5);

    private final int code;

    ExportStatus(int code) {
        this.code = code;
    }

    /**
     * DB の INT コード。
     *
     * @return INT コード
     */
    public int getCode() {
        return code;
    }

    /**
     * INT コードから enum を得る。
     *
     * @param code DB の INT コード（null 可）
     * @return 対応する enum。code が null なら null
     * @throws IllegalArgumentException 未知のコードの場合
     */
    public static ExportStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ExportStatus v : values()) {
            if (v.code == code) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown ExportStatus code: " + code);
    }
}

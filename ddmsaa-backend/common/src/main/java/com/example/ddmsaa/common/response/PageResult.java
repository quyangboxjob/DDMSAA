package com.example.ddmsaa.common.response;

import java.util.List;

/**
 * ページング結果（spec-07 の PageResult）。
 *
 * @param records 当該ページのレコード（null は生成時に空リストへ正規化される）
 * @param total 総件数
 * @param pageNum ページ番号
 * @param pageSize 1 ページあたりの件数
 * @param pages 総ページ数
 * @param <T> レコードの型
 */
public record PageResult<T>(List<T> records, long total, int pageNum, int pageSize, int pages) {

    /**
     * ページング結果を生成し、総ページ数を算出する。
     *
     * @param records 当該ページのレコード（null 可。null なら空リスト）
     * @param total 総件数（0 以上）
     * @param pageNum ページ番号
     * @param pageSize 1 ページあたりの件数（1 以上）
     * @param <T> レコードの型
     * @return ページング結果
     * @throws IllegalArgumentException pageSize が 0 以下、または total が負の場合
     */
    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive: " + pageSize);
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative: " + total);
        }
        List<T> safeRecords = records == null ? List.of() : records;
        int pages = (int) ((total + pageSize - 1) / pageSize);
        return new PageResult<>(safeRecords, total, pageNum, pageSize, pages);
    }
}

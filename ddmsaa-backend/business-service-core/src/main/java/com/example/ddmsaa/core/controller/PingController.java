package com.example.ddmsaa.core.controller;

import com.example.ddmsaa.common.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 疎通確認用のスケルトン Controller。
 *
 * <p>業務ロジックを持たない。認証必須（permitAll は {@code /api/v1/auth/**} と
 * {@code /actuator/**} のみ）のため、Gateway 経由・JWT 検証を含む疎通確認に使える。</p>
 */
@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    /** 疎通確認の応答値。 */
    static final String PONG = "pong";

    /**
     * 疎通確認に応答する。
     *
     * @return {@code "pong"} を {@link Result} でラップした応答
     */
    @GetMapping
    public Result<String> ping() {
        return Result.ok(PONG);
    }
}

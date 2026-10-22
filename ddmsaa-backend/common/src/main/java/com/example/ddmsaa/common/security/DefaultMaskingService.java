package com.example.ddmsaa.common.security;

import org.springframework.stereotype.Component;

/**
 * 既定の {@link MaskingService}。マスクを行わず対象をそのまま返す。
 *
 * <p>機能側が独自の実装 Bean を提供することで差し替える。
 */
@Component
public class DefaultMaskingService implements MaskingService {

    /** {@inheritDoc} */
    @Override
    public <T> T maskByRole(T target, String viewerRole, boolean isSelf) {
        return target;
    }
}

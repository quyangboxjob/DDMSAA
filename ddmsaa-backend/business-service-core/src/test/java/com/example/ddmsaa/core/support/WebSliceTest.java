package com.example.ddmsaa.core.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

/**
 * L2（Web スライス）テスト用の合成アノテーション。{@code @WebMvcTest} + test プロファイル +
 * 本番相当のセキュリティ設定（{@link WebSliceTestConfig}）。使い方：
 *
 * <pre>
 * &#064;WebSliceTest(controllers = OrderController.class)
 * class OrderControllerTest {
 *     &#064;Autowired MockMvc mockMvc;
 *     &#064;Autowired TestJwt jwt;
 *     &#064;MockitoBean OrderService orderService;
 * }
 * </pre>
 *
 * <p>呼び出しは {@code .header("Authorization", jwt.bearer(userId, Role.ADMIN))} のように
 * 本物の JWT フィルタを通す。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest
@ActiveProfiles("test")
@Import(WebSliceTestConfig.class)
public @interface WebSliceTest {

    /**
     * テスト対象の Controller。
     *
     * @return Controller クラス
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}

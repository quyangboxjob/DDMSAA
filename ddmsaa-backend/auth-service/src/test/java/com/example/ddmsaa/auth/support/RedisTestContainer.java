package com.example.ddmsaa.auth.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** L3 用の Redis コンテナ（Magic Link の一時状態用）。JVM ごとに 1 つ起動して共有する。 */
public final class RedisTestContainer {

    /** イメージ（spec-01 §2.3：Redis 7.x）。 */
    private static final DockerImageName IMAGE = DockerImageName.parse("redis:7");

    private static final int REDIS_PORT = 6379;

    private static GenericContainer<?> container;

    private RedisTestContainer() {
    }

    /**
     * コンテナを起動して返す（2 回目以降は起動済みのものを返す）。
     *
     * @return 起動済みのコンテナ
     */
    @SuppressWarnings("resource") // JVM 全体で共有し、終了時に Ryuk が片付ける
    public static synchronized GenericContainer<?> get() {
        if (container == null) {
            GenericContainer<?> created =
                    new GenericContainer<>(IMAGE).withExposedPorts(REDIS_PORT);
            created.start();
            container = created;
        }
        return container;
    }

    /**
     * Redis の接続先をコンテナへ向ける（{@code @DynamicPropertySource} から呼ぶ）。
     *
     * @param registry プロパティの登録先
     */
    public static void register(DynamicPropertyRegistry registry) {
        GenericContainer<?> redis = get();
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_PORT));
    }
}

package com.example.ddmsaa.core.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * L3（プロセス内結合）用の PostgreSQL コンテナ。JVM ごとに 1 つ起動して全テストで共有する。
 *
 * <p>スキーマは本番相当の機構A と同じ SQL（{@code 基本設計/データベース/setup/} の
 * {@code 10_schema.sql}・{@code 20_seed.sql}）を初期化スクリプトとして流して作る。
 * テスト専用の DDL を別に持たない。V1 以降の Flyway は各サービスの起動時に適用される。</p>
 */
public final class PostgresTestContainer {

    /** イメージ（spec-01 §2.3：PostgreSQL 16）。 */
    private static final String IMAGE = "postgres:16";

    /** 機構A の SQL の相対位置（リポジトリルート基準）。 */
    private static final Path SETUP_DIR = Path.of("docs", "基本設計", "データベース", "setup");

    /** コンテナに流す機構A の SQL（実行順）。サンプルデータ（21_*）は流さない。 */
    private static final List<String> INIT_SCRIPTS = List.of("10_schema.sql", "20_seed.sql");

    private static PostgreSQLContainer<?> container;

    private PostgresTestContainer() {
    }

    /**
     * コンテナを起動して返す（2 回目以降は起動済みのものを返す）。
     *
     * @return 起動済みのコンテナ
     */
    @SuppressWarnings("resource") // JVM 全体で共有し、終了時に Ryuk が片付ける
    public static synchronized PostgreSQLContainer<?> get() {
        if (container == null) {
            PostgreSQLContainer<?> created = new PostgreSQLContainer<>(IMAGE)
                    .withDatabaseName("ddmsaa")
                    .withUsername("ddmsaa")
                    .withPassword("test-only-password")
                    .withEnv("TZ", "Asia/Tokyo")
                    .withCommand("postgres", "-c", "timezone=Asia/Tokyo");
            Path setupDir = locateRepositoryRoot().resolve(SETUP_DIR);
            for (String script : INIT_SCRIPTS) {
                created.withCopyFileToContainer(MountableFile.forHostPath(setupDir.resolve(script)),
                        "/docker-entrypoint-initdb.d/" + script);
            }
            created.start();
            container = created;
        }
        return container;
    }

    /**
     * データソースの接続先をコンテナへ向ける（{@code @DynamicPropertySource} から呼ぶ）。
     *
     * @param registry プロパティの登録先
     */
    public static void register(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> postgres = get();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * カレントディレクトリから親をたどって、機構A の SQL があるリポジトリルートを探す。
     *
     * @return リポジトリルート
     * @throws IllegalStateException 見つからない場合
     */
    static Path locateRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve(SETUP_DIR).resolve(INIT_SCRIPTS.get(0)))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException(
                "setup SQL not found above " + Path.of("").toAbsolutePath() + ": " + SETUP_DIR);
    }
}

package com.example.ddmsaa.common.message;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * クラスパス上の {@code messages/*.yaml} を起動時に読み込む {@link MessageCatalog} 実装。
 *
 * <p>日本語正本を優先し、言語バリアント（{@code *.zh.yaml} 等）は未登録の ID のみ補う。
 * ロード後は不変でスレッドセーフ。
 */
@Component
public class YamlMessageCatalog implements MessageCatalog {

    private static final Logger log = LoggerFactory.getLogger(YamlMessageCatalog.class);

    private static final String LOCATION_PATTERN = "classpath*:messages/*.yaml";

    private static final String TOP_LEVEL_KEY = "messages";

    private static final String MESSAGE_KEY = "message";

    /** 言語バリアント（例: common-messages.zh.yaml）のファイル名判定。 */
    private static final Pattern VARIANT_FILE =
            Pattern.compile(".+\\.[A-Za-z]{2}(-[A-Za-z]+)?\\.yaml");

    /** ロード済みマップ。init() で一度だけ差し替え、以降は不変。 */
    private volatile Map<String, String> messages = Map.of();

    /** Spring による生成用の既定コンストラクタ（ロードは {@link #init()} で行う）。 */
    public YamlMessageCatalog() {
    }

    /**
     * 起動時に messages/*.yaml を全件ロードする。失敗時は起動を停止する。
     */
    @PostConstruct
    void init() {
        try {
            this.messages = Map.copyOf(load());
        } catch (Exception e) {
            log.error("メッセージカタログのロードに失敗しました", e);
            throw new IllegalStateException(
                    "Failed to load message catalog: " + LOCATION_PATTERN, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(String messageId, Object... args) {
        if (messageId == null) {
            return null;
        }
        String template = messages.get(messageId);
        if (template == null) {
            return messageId;
        }
        if (args == null) {
            return template;
        }
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(String messageId) {
        return messageId != null && messages.containsKey(messageId);
    }

    private Map<String, String> load() throws IOException {
        Resource[] resources =
                new PathMatchingResourcePatternResolver().getResources(LOCATION_PATTERN);
        if (resources.length == 0) {
            throw new IllegalStateException("No message catalog found: " + LOCATION_PATTERN);
        }
        List<Resource> canonical = new ArrayList<>();
        List<Resource> variants = new ArrayList<>();
        for (Resource resource : resources) {
            if (VARIANT_FILE.matcher(fileName(resource)).matches()) {
                variants.add(resource);
            } else {
                canonical.add(resource);
            }
        }
        Comparator<Resource> byName = Comparator.comparing(YamlMessageCatalog::fileName);
        canonical.sort(byName);
        variants.sort(byName);
        Map<String, String> loaded = new HashMap<>();
        for (Resource resource : canonical) {
            loaded.putAll(parse(resource));
        }
        // 言語バリアントは正本（日本語）を上書きしない
        for (Resource resource : variants) {
            parse(resource).forEach(loaded::putIfAbsent);
        }
        return loaded;
    }

    private static String fileName(Resource resource) {
        String name = resource.getFilename();
        return name == null ? "" : name;
    }

    private Map<String, String> parse(Resource resource) throws IOException {
        Object root;
        try (InputStream in = resource.getInputStream()) {
            root = new Yaml().load(in);
        }
        if (!(root instanceof Map<?, ?> rootMap)
                || !(rootMap.get(TOP_LEVEL_KEY) instanceof Map<?, ?> entries)) {
            throw new IllegalStateException(
                    "Invalid message catalog: " + resource.getDescription());
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> value)
                    || !(value.get(MESSAGE_KEY) instanceof String text)) {
                throw new IllegalStateException("Invalid message entry " + entry.getKey()
                        + " in " + resource.getDescription());
            }
            result.put(String.valueOf(entry.getKey()), text);
        }
        return result;
    }
}

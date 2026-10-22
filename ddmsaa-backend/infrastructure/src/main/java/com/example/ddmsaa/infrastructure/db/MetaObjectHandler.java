package com.example.ddmsaa.infrastructure.db;

import com.example.ddmsaa.infrastructure.jwt.UserContext;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

/**
 * MyBatis Interceptor として監査列を自動設定するハンドラ。
 *
 * <p>INSERT では createdBy / updatedBy を、UPDATE では updatedBy を設定する。実行者は
 * {@link UserContext#getUserId()} とし、未設定（バッチ等）の場合は {@code SYSTEM} を用いる。
 * 対象エンティティにフィールドが無い場合は何もしない。</p>
 *
 * <p>日時列（created_at / updated_at）は設定しない。DB が正本で、INSERT は列の既定値
 * （CURRENT_TIMESTAMP）、UPDATE は {@code trg_set_updated_at_*} トリガーが設定する。Java 側の
 * 時刻は DB の値と一致せず、楽観ロックの照合値に使うと誤った競合（E0051）になるため、
 * Mapper の INSERT / UPDATE 文は日時列を指定しない。更新後の値は再読込で得る。</p>
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class})
})
public class MetaObjectHandler implements Interceptor {

    private static final String SYSTEM_USER = "SYSTEM";

    /**
     * ハンドラを生成する。
     */
    public MetaObjectHandler() {
    }

    /**
     * INSERT / UPDATE の実行前に監査列を設定し、本来の処理を続行する。
     *
     * @param invocation MyBatis の呼び出し情報（args[0]=MappedStatement、args[1]=パラメータ）
     * @return 本来の処理の戻り値
     * @throws Throwable 本来の処理が送出した例外
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Object parameter = args.length > 1 ? args[1] : null;
        if (parameter != null && args[0] instanceof MappedStatement statement) {
            SqlCommandType type = statement.getSqlCommandType();
            if (type == SqlCommandType.INSERT || type == SqlCommandType.UPDATE) {
                String userId = UserContext.getUserId();
                String actor = userId != null ? userId : SYSTEM_USER;
                apply(parameter, type == SqlCommandType.INSERT, actor,
                        Collections.newSetFromMap(new IdentityHashMap<>()));
            }
        }
        return invocation.proceed();
    }

    private void apply(Object target, boolean insert, String actor, Set<Object> visited) {
        if (target == null || !visited.add(target)) {
            return;
        }
        if (target instanceof Map<?, ?> map) {
            map.values().forEach(v -> apply(v, insert, actor, visited));
            return;
        }
        if (target instanceof Collection<?> collection) {
            collection.forEach(v -> apply(v, insert, actor, visited));
            return;
        }
        if (target.getClass().getName().startsWith("java.") || target.getClass().isArray()) {
            return;
        }
        if (insert) {
            setFieldIfExists(target, "createdBy", actor);
        }
        setFieldIfExists(target, "updatedBy", actor);
    }

    private void setFieldIfExists(Object target, String name, Object value) {
        for (Class<?> c = target.getClass(); c != null && c != Object.class;
                c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                if (field.getType().isAssignableFrom(value.getClass())) {
                    field.setAccessible(true);
                    field.set(target, value);
                }
                return;
            } catch (NoSuchFieldException e) {
                continue;
            } catch (IllegalAccessException | RuntimeException e) {
                return;
            }
        }
    }
}

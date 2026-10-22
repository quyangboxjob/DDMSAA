package com.example.ddmsaa.infrastructure.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.ddmsaa.infrastructure.jwt.UserContext;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MetaObjectHandler の監査列設定")
class MetaObjectHandlerTest {

    private final MetaObjectHandler handler = new MetaObjectHandler();

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    static class Base {
        OffsetDateTime createdAt;
        String createdBy;
    }

    static class Entity extends Base {
        String name;
        OffsetDateTime updatedAt;
        String updatedBy;
    }

    static class NoAudit {
        String name;
    }

    private static Invocation invocation(SqlCommandType type, Object parameter, Object[] proceeded)
            throws Exception {
        MappedStatement statement = mock(MappedStatement.class);
        when(statement.getSqlCommandType()).thenReturn(type);
        Method update = Executor.class.getMethod("update", MappedStatement.class, Object.class);
        Executor executor = mock(Executor.class);
        when(executor.update(statement, parameter)).thenAnswer(i -> {
            proceeded[0] = "called";
            return 1;
        });
        return new Invocation(executor, update, new Object[] {statement, parameter});
    }

    @Test
    @DisplayName("INSERT では 4 つの監査列を UserContext のユーザーで設定する（親クラスの項目も含む）")
    void insertSetsAllAuditColumns() throws Throwable {
        UserContext.set("E0001", "EMPLOYEE");
        Entity entity = new Entity();
        Object[] proceeded = new Object[1];

        Object result = handler.intercept(invocation(SqlCommandType.INSERT, entity, proceeded));

        assertThat(result).isEqualTo(1);
        assertThat(proceeded[0]).isEqualTo("called");
        assertThat(entity.createdAt).isNotNull();
        assertThat(entity.updatedAt).isEqualTo(entity.createdAt);
        assertThat(entity.createdBy).isEqualTo("E0001");
        assertThat(entity.updatedBy).isEqualTo("E0001");
    }

    @Test
    @DisplayName("UPDATE では updatedAt / updatedBy だけを設定し created 系は変更しない")
    void updateSetsOnlyUpdatedColumns() throws Throwable {
        UserContext.set("A0001", "ADMIN");
        Entity entity = new Entity();
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.UPDATE, entity, proceeded));

        assertThat(entity.updatedAt).isNotNull();
        assertThat(entity.updatedBy).isEqualTo("A0001");
        assertThat(entity.createdAt).isNull();
        assertThat(entity.createdBy).isNull();
    }

    @Test
    @DisplayName("UserContext が未設定なら SYSTEM を使い、処理は継続する")
    void fallsBackToSystem() throws Throwable {
        Entity entity = new Entity();
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.INSERT, entity, proceeded));

        assertThat(entity.createdBy).isEqualTo("SYSTEM");
        assertThat(entity.updatedBy).isEqualTo("SYSTEM");
        assertThat(proceeded[0]).isEqualTo("called");
    }

    @Test
    @DisplayName("parameter が null なら何も設定せず proceed する")
    void nullParameterProceeds() throws Throwable {
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.INSERT, null, proceeded));

        assertThat(proceeded[0]).isEqualTo("called");
    }

    @Test
    @DisplayName("対象に該当フィールドが無くても例外にならない")
    void missingFieldsAreSkipped() throws Throwable {
        NoAudit entity = new NoAudit();
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.INSERT, entity, proceeded));

        assertThat(proceeded[0]).isEqualTo("called");
    }

    @Test
    @DisplayName("INSERT / UPDATE 以外の命令では設定しない")
    void otherCommandsAreIgnored() throws Throwable {
        Entity entity = new Entity();
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.DELETE, entity, proceeded));

        assertThat(entity.updatedAt).isNull();
        assertThat(proceeded[0]).isEqualTo("called");
    }

    @Test
    @DisplayName("Map や List で渡された複数エンティティにも設定する")
    void mapAndCollectionParameters() throws Throwable {
        Entity first = new Entity();
        Entity second = new Entity();
        Map<String, Object> param = new HashMap<>();
        param.put("list", List.of(first, second));
        param.put("label", "text");
        Object[] proceeded = new Object[1];

        handler.intercept(invocation(SqlCommandType.INSERT, param, proceeded));

        assertThat(first.createdBy).isEqualTo("SYSTEM");
        assertThat(second.updatedAt).isNotNull();
    }
}

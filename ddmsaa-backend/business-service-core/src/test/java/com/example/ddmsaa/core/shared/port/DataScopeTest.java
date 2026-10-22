package com.example.ddmsaa.core.shared.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.common.enums.Role;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataScope のデータ範囲")
class DataScopeTest {

    @Test
    @DisplayName("ADMIN は isAll() が true")
    void adminIsAll() {
        DataScope scope = new DataScope(Role.ADMIN, "A0001", Set.of());

        assertThat(scope.isAll()).isTrue();
    }

    @Test
    @DisplayName("EMPLOYEE と CUSTOMER は isAll() が false")
    void nonAdminIsNotAll() {
        assertThat(new DataScope(Role.EMPLOYEE, "E0001", Set.of("CU00000001")).isAll())
                .isFalse();
        assertThat(new DataScope(Role.CUSTOMER, "C0001", Set.of("CU00000001")).isAll())
                .isFalse();
    }

    @Test
    @DisplayName("customerIds が null なら空集合になる")
    void nullCustomerIdsBecomesEmpty() {
        DataScope scope = new DataScope(Role.EMPLOYEE, "E0001", null);

        assertThat(scope.customerIds()).isEmpty();
    }

    @Test
    @DisplayName("customerIds は防御的にコピーされ変更できない")
    void customerIdsAreImmutableCopy() {
        Set<String> source = new HashSet<>(Set.of("CU00000001"));
        DataScope scope = new DataScope(Role.EMPLOYEE, "E0001", source);
        source.add("CU00000002");

        assertThat(scope.customerIds()).containsExactly("CU00000001");
        assertThatThrownBy(() -> scope.customerIds().add("CU00000003"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("role・userId を保持する")
    void keepsRoleAndUserId() {
        DataScope scope = new DataScope(Role.CUSTOMER, "C0001", Set.of("CU00000001"));

        assertThat(scope.role()).isEqualTo(Role.CUSTOMER);
        assertThat(scope.userId()).isEqualTo("C0001");
    }
}

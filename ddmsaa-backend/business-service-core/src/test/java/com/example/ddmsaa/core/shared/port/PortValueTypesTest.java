package com.example.ddmsaa.core.shared.port;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.core.shared.enums.CustomerStatus;
import com.example.ddmsaa.core.shared.enums.OrderStatus;
import com.example.ddmsaa.core.shared.enums.ProductStatus;
import com.example.ddmsaa.core.shared.enums.TaxCategory;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ポートの値型（record）")
class PortValueTypesTest {

    @Test
    @DisplayName("OrderTransition は各項目を保持する")
    void orderTransition() {
        OrderTransition t = new OrderTransition("OR00000001", "CU00000001", "E0001",
                OrderStatus.PENDING_APPROVAL, OrderStatus.APPROVED);

        assertThat(t.orderId()).isEqualTo("OR00000001");
        assertThat(t.customerId()).isEqualTo("CU00000001");
        assertThat(t.salesUserId()).isEqualTo("E0001");
        assertThat(t.fromStatus()).isEqualTo(OrderStatus.PENDING_APPROVAL);
        assertThat(t.toStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("OrderAccess は各項目を保持する")
    void orderAccess() {
        OrderAccess a = new OrderAccess("OR00000001", "CU00000001", "E0001", OrderStatus.DRAFT);

        assertThat(a.id()).isEqualTo("OR00000001");
        assertThat(a.customerId()).isEqualTo("CU00000001");
        assertThat(a.salesUserId()).isEqualTo("E0001");
        assertThat(a.status()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("PendingApproval と CustomerInfo は各項目を保持する")
    void pendingApprovalAndCustomerInfo() {
        PendingApproval p = new PendingApproval("AR00000001", "E0001");
        CustomerInfo c = new CustomerInfo("CU00000001", "テスト顧客", CustomerStatus.ACTIVE);

        assertThat(p.approvalRequestId()).isEqualTo("AR00000001");
        assertThat(p.createdBy()).isEqualTo("E0001");
        assertThat(c.id()).isEqualTo("CU00000001");
        assertThat(c.name()).isEqualTo("テスト顧客");
        assertThat(c.status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    @DisplayName("ProductSnapshot は各項目を保持する")
    void productSnapshot() {
        ProductSnapshot s = new ProductSnapshot("PR00000001", "P-001", "テスト商品",
                new BigDecimal("1000.00"), TaxCategory.STANDARD, new BigDecimal("0.10"),
                ProductStatus.ACTIVE);

        assertThat(s.id()).isEqualTo("PR00000001");
        assertThat(s.code()).isEqualTo("P-001");
        assertThat(s.name()).isEqualTo("テスト商品");
        assertThat(s.standardUnitPrice()).isEqualByComparingTo("1000");
        assertThat(s.taxCategory()).isEqualTo(TaxCategory.STANDARD);
        assertThat(s.taxRate()).isEqualByComparingTo("0.10");
        assertThat(s.status()).isEqualTo(ProductStatus.ACTIVE);
    }
}

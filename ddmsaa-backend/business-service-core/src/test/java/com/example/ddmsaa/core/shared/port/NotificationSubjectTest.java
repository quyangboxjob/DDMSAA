package com.example.ddmsaa.core.shared.port;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.core.shared.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationSubject のファクトリ")
class NotificationSubjectTest {

    @Test
    @DisplayName("forExport は exportJobId だけを持つ")
    void forExport() {
        NotificationSubject subject = NotificationSubject.forExport("EJ00000001");

        assertThat(subject.exportJobId()).isEqualTo("EJ00000001");
        assertThat(subject.orderId()).isNull();
        assertThat(subject.customerId()).isNull();
        assertThat(subject.salesUserId()).isNull();
        assertThat(subject.applicantUserId()).isNull();
        assertThat(subject.fromStatus()).isNull();
    }

    @Test
    @DisplayName("forOrder は注文系の項目を持ち exportJobId は null")
    void forOrder() {
        NotificationSubject subject = NotificationSubject.forOrder("OR00000001", "CU00000001",
                "E0001", "E0002", OrderStatus.APPROVED);

        assertThat(subject.orderId()).isEqualTo("OR00000001");
        assertThat(subject.customerId()).isEqualTo("CU00000001");
        assertThat(subject.salesUserId()).isEqualTo("E0001");
        assertThat(subject.applicantUserId()).isEqualTo("E0002");
        assertThat(subject.fromStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(subject.exportJobId()).isNull();
    }

    @Test
    @DisplayName("forOrder は applicantUserId と fromStatus に null を渡せる")
    void forOrderAllowsNulls() {
        NotificationSubject subject = NotificationSubject.forOrder("OR00000001", "CU00000001",
                "E0001", null, null);

        assertThat(subject.applicantUserId()).isNull();
        assertThat(subject.fromStatus()).isNull();
    }
}

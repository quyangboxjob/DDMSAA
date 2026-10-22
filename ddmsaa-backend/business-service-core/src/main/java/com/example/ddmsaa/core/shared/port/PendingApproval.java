package com.example.ddmsaa.core.shared.port;

/**
 * 承認待ち（PENDING）の承認依頼。{@link ApprovalRequestPort#findPending} の戻り値。
 *
 * @param approvalRequestId 承認依頼 ID
 * @param createdBy 承認依頼の作成者（申請者）のユーザー ID
 */
public record PendingApproval(String approvalRequestId, String createdBy) {
}

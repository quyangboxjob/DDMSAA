/**
 * 列挙型（T07-1〜T07-13）。値は spec-07-api-list.yaml の components.schemas と
 * 開発規約/spec-data-dictionary-project-temp.md の英語名に一致させる。
 * TypeScript の `enum` は使わず、定数配列から Union Type を導出する。
 */

/** ロール（T07-1）。 */
export const ROLES = ['ADMIN', 'EMPLOYEE', 'CUSTOMER'] as const
export type Role = (typeof ROLES)[number]

/** 利用者状態（T07-2）。 */
export const USER_STATUSES = ['ACTIVE', 'DISABLED'] as const
export type UserStatus = (typeof USER_STATUSES)[number]

/** 顧客状態（T07-3）。 */
export const CUSTOMER_STATUSES = ['ACTIVE', 'INACTIVE'] as const
export type CustomerStatus = (typeof CUSTOMER_STATUSES)[number]

/** 商品状態（T07-4）。 */
export const PRODUCT_STATUSES = ['ACTIVE', 'INACTIVE'] as const
export type ProductStatus = (typeof PRODUCT_STATUSES)[number]

/** 税区分（T07-5）。 */
export const TAX_CATEGORIES = ['STANDARD', 'REDUCED', 'EXEMPT'] as const
export type TaxCategory = (typeof TAX_CATEGORIES)[number]

/** 注文状態（T07-6）。 */
export const ORDER_STATUSES = [
  'DRAFT',
  'PENDING_APPROVAL',
  'RETURNED',
  'APPROVED',
  'CANCELLED',
] as const
export type OrderStatus = (typeof ORDER_STATUSES)[number]

/** 承認依頼状態（T07-7）。 */
export const APPROVAL_REQUEST_STATUSES = ['PENDING', 'APPROVED', 'RETURNED'] as const
export type ApprovalRequestStatus = (typeof APPROVAL_REQUEST_STATUSES)[number]

/** 承認操作（T07-8）。 */
export const APPROVAL_ACTIONS = ['APPROVE', 'RETURN'] as const
export type ApprovalAction = (typeof APPROVAL_ACTIONS)[number]

/** 添付公開範囲（T07-9）。 */
export const ATTACHMENT_VISIBILITIES = ['INTERNAL', 'CUSTOMER'] as const
export type AttachmentVisibility = (typeof ATTACHMENT_VISIBILITIES)[number]

/** スキャン状態（T07-10）。 */
export const SCAN_STATUSES = ['PENDING', 'SAFE', 'REJECTED'] as const
export type ScanStatus = (typeof SCAN_STATUSES)[number]

/** 通知イベント（T07-11）。 */
export const NOTIFICATION_EVENTS = [
  'ORDER_SUBMITTED',
  'ORDER_APPROVED',
  'ORDER_RETURNED',
  'ORDER_CANCELLED',
  'EXPORT_COMPLETED',
] as const
export type NotificationEvent = (typeof NOTIFICATION_EVENTS)[number]

/** 通知状態（T07-12）。 */
export const NOTIFICATION_STATUSES = ['QUEUED', 'SENDING', 'SENT', 'FAILED'] as const
export type NotificationStatus = (typeof NOTIFICATION_STATUSES)[number]

/** 出力状態（T07-13）。 */
export const EXPORT_STATUSES = ['QUEUED', 'RUNNING', 'READY', 'FAILED', 'EXPIRED'] as const
export type ExportStatus = (typeof EXPORT_STATUSES)[number]

/** 列挙名 → 値の Union。表示名の解決（i18n の `ui.enums.<EnumName>.<VALUE>`）に使う。 */
export interface EnumValueMap {
  Role: Role
  UserStatus: UserStatus
  CustomerStatus: CustomerStatus
  ProductStatus: ProductStatus
  TaxCategory: TaxCategory
  OrderStatus: OrderStatus
  ApprovalRequestStatus: ApprovalRequestStatus
  ApprovalAction: ApprovalAction
  AttachmentVisibility: AttachmentVisibility
  ScanStatus: ScanStatus
  NotificationEvent: NotificationEvent
  NotificationStatus: NotificationStatus
  ExportStatus: ExportStatus
}

/** 列挙名の Union。 */
export type EnumName = keyof EnumValueMap

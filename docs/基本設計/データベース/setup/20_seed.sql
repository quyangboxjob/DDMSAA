-- 20_seed.sql：参照マスタの初期値（T07-1〜T07-13 の 13 コード表と tax_rates）。
-- 生成元：開発規約/spec-data-dictionary-project-temp.md（T07）、spec-06-db-physical.yaml master_data。
-- Flyway V1（初期管理者・承認ルール）の外部キーが参照するため、V1 より前に投入する。冪等（ON CONFLICT DO NOTHING）。
-- 以降の変更は Flyway の R__ スクリプト（upsert）が行う。

-- T07-1：user_role_codes
INSERT INTO public.user_role_codes (user_role_code, user_role_name) VALUES
    (1, 'ADMIN'),
    (2, 'EMPLOYEE'),
    (3, 'CUSTOMER')
ON CONFLICT DO NOTHING;

-- T07-2：user_status_codes
INSERT INTO public.user_status_codes (user_status_code, user_status_name) VALUES
    (1, 'ACTIVE'),
    (2, 'DISABLED')
ON CONFLICT DO NOTHING;

-- T07-3：customer_status_codes
INSERT INTO public.customer_status_codes (customer_status_code, customer_status_name) VALUES
    (1, 'ACTIVE'),
    (2, 'INACTIVE')
ON CONFLICT DO NOTHING;

-- T07-4：product_status_codes
INSERT INTO public.product_status_codes (product_status_code, product_status_name) VALUES
    (1, 'ACTIVE'),
    (2, 'INACTIVE')
ON CONFLICT DO NOTHING;

-- T07-5：tax_category_codes
INSERT INTO public.tax_category_codes (tax_category_code, tax_category_name) VALUES
    (1, 'STANDARD'),
    (2, 'REDUCED'),
    (3, 'EXEMPT')
ON CONFLICT DO NOTHING;

-- T07-6：order_status_codes
INSERT INTO public.order_status_codes (order_status_code, order_status_name) VALUES
    (1, 'DRAFT'),
    (2, 'PENDING_APPROVAL'),
    (3, 'RETURNED'),
    (4, 'APPROVED'),
    (5, 'CANCELLED')
ON CONFLICT DO NOTHING;

-- T07-7：approval_request_status_codes
INSERT INTO public.approval_request_status_codes (approval_request_status_code, approval_request_status_name) VALUES
    (1, 'PENDING'),
    (2, 'APPROVED'),
    (3, 'RETURNED')
ON CONFLICT DO NOTHING;

-- T07-8：approval_action_codes
INSERT INTO public.approval_action_codes (approval_action_code, approval_action_name) VALUES
    (1, 'APPROVE'),
    (2, 'RETURN')
ON CONFLICT DO NOTHING;

-- T07-9：attachment_visibility_codes
INSERT INTO public.attachment_visibility_codes (attachment_visibility_code, attachment_visibility_name) VALUES
    (1, 'INTERNAL'),
    (2, 'CUSTOMER')
ON CONFLICT DO NOTHING;

-- T07-10：scan_status_codes
INSERT INTO public.scan_status_codes (scan_status_code, scan_status_name) VALUES
    (1, 'PENDING'),
    (2, 'SAFE'),
    (3, 'REJECTED')
ON CONFLICT DO NOTHING;

-- T07-11：notification_event_codes
INSERT INTO public.notification_event_codes (notification_event_code, notification_event_name) VALUES
    (1, 'ORDER_SUBMITTED'),
    (2, 'ORDER_APPROVED'),
    (3, 'ORDER_RETURNED'),
    (4, 'ORDER_CANCELLED'),
    (5, 'EXPORT_COMPLETED')
ON CONFLICT DO NOTHING;

-- T07-12：notification_status_codes
INSERT INTO public.notification_status_codes (notification_status_code, notification_status_name) VALUES
    (1, 'QUEUED'),
    (2, 'SENDING'),
    (3, 'SENT'),
    (4, 'FAILED')
ON CONFLICT DO NOTHING;

-- T07-13：export_status_codes
INSERT INTO public.export_status_codes (export_status_code, export_status_name) VALUES
    (1, 'QUEUED'),
    (2, 'RUNNING'),
    (3, 'READY'),
    (4, 'FAILED'),
    (5, 'EXPIRED')
ON CONFLICT DO NOTHING;

-- INI-02：税率（BR-ORD-004）
INSERT INTO public.tax_rates (tax_category, tax_rate) VALUES
    (1, 10.00),
    (2, 8.00),
    (3, 0.00)
ON CONFLICT DO NOTHING;

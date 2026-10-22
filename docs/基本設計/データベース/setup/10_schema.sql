-- 10_schema.sql：機構A 初期 schema（31 テーブル）。
-- 生成元：基本設計/共通/spec-06-db-physical.yaml（構造変更は 06 を先に改訂してから再生成する）。
-- 実行：docker/postgres/init/00-setup.sh が DB_USER で 1 回だけ実行する。Flyway のバージョン管理対象外（baseline=0）。

-- ---------------------------------------------------------------------
-- 共有関数（開発規約/spec-database-rule.md §4.1-SEQ / §4.3-Trigger）
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.next_business_id(p_prefix TEXT, p_width INT DEFAULT 4)
RETURNS VARCHAR AS $$
DECLARE
    v_seq TEXT := 'public.seq_id_' || lower(p_prefix);
    v_num BIGINT;
BEGIN
    v_num := nextval(v_seq::regclass);
    RETURN p_prefix || lpad(v_num::text, p_width, '0');
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- 採番シーケンス（prefix ごとに 1 本。spec-database-rule.md §4.1-SEQ の割当表）
-- seq_id_a は初期管理者 A0001 を Flyway V1 で固定投入するため 2 から開始する（TBD-MIG-02）。
-- ---------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS public.seq_id_a START WITH 2 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_e START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_c START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_cs START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_cc START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_as START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_pr START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_od START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_oi START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_oh START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_ar START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_aq START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_ah START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_at START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_ej START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_nl START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS public.seq_id_al START WITH 1 INCREMENT BY 1 NO CYCLE;

-- ---------------------------------------------------------------------
-- テーブル（外部キーの依存順：コードテーブル → 業務テーブル）
-- ---------------------------------------------------------------------

-- TBL-user-role-codes：ロールコード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.user_role_codes (
    user_role_code INT NOT NULL,
    user_role_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_user_role_codes PRIMARY KEY (user_role_code),
    CONSTRAINT uq_user_role_codes_name UNIQUE (user_role_name)
);

-- TBL-user-status-codes：利用者状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.user_status_codes (
    user_status_code INT NOT NULL,
    user_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_user_status_codes PRIMARY KEY (user_status_code),
    CONSTRAINT uq_user_sts_codes_name UNIQUE (user_status_name)
);

-- TBL-customer-status-codes：顧客状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.customer_status_codes (
    customer_status_code INT NOT NULL,
    customer_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_customer_status_codes PRIMARY KEY (customer_status_code),
    CONSTRAINT uq_cus_sts_codes_name UNIQUE (customer_status_name)
);

-- TBL-product-status-codes：商品状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.product_status_codes (
    product_status_code INT NOT NULL,
    product_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_product_status_codes PRIMARY KEY (product_status_code),
    CONSTRAINT uq_prd_sts_codes_name UNIQUE (product_status_name)
);

-- TBL-tax-category-codes：税区分コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.tax_category_codes (
    tax_category_code INT NOT NULL,
    tax_category_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_tax_category_codes PRIMARY KEY (tax_category_code),
    CONSTRAINT uq_tax_cat_codes_name UNIQUE (tax_category_name)
);

-- TBL-order-status-codes：注文状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.order_status_codes (
    order_status_code INT NOT NULL,
    order_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_order_status_codes PRIMARY KEY (order_status_code),
    CONSTRAINT uq_ord_sts_codes_name UNIQUE (order_status_name)
);

-- TBL-approval-request-status-codes：承認依頼状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.approval_request_status_codes (
    approval_request_status_code INT NOT NULL,
    approval_request_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_apr_req_sts_codes PRIMARY KEY (approval_request_status_code),
    CONSTRAINT uq_apr_req_sts_codes_name UNIQUE (approval_request_status_name)
);

-- TBL-approval-action-codes：承認操作コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.approval_action_codes (
    approval_action_code INT NOT NULL,
    approval_action_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_approval_action_codes PRIMARY KEY (approval_action_code),
    CONSTRAINT uq_apr_action_codes_name UNIQUE (approval_action_name)
);

-- TBL-attachment-visibility-codes：添付公開範囲コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.attachment_visibility_codes (
    attachment_visibility_code INT NOT NULL,
    attachment_visibility_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_attachment_visibility_codes PRIMARY KEY (attachment_visibility_code),
    CONSTRAINT uq_att_vis_codes_name UNIQUE (attachment_visibility_name)
);

-- TBL-scan-status-codes：スキャン状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.scan_status_codes (
    scan_status_code INT NOT NULL,
    scan_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_scan_status_codes PRIMARY KEY (scan_status_code),
    CONSTRAINT uq_scan_sts_codes_name UNIQUE (scan_status_name)
);

-- TBL-notification-event-codes：通知イベントコード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.notification_event_codes (
    notification_event_code INT NOT NULL,
    notification_event_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_notification_event_codes PRIMARY KEY (notification_event_code),
    CONSTRAINT uq_ntf_evt_codes_name UNIQUE (notification_event_name)
);

-- TBL-notification-status-codes：通知状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.notification_status_codes (
    notification_status_code INT NOT NULL,
    notification_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_notification_status_codes PRIMARY KEY (notification_status_code),
    CONSTRAINT uq_ntf_sts_codes_name UNIQUE (notification_status_name)
);

-- TBL-export-status-codes：出力状態コード
-- no soft delete: コードテーブル
CREATE TABLE IF NOT EXISTS public.export_status_codes (
    export_status_code INT NOT NULL,
    export_status_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_export_status_codes PRIMARY KEY (export_status_code),
    CONSTRAINT uq_exp_sts_codes_name UNIQUE (export_status_name)
);

-- TBL-customers：顧客
CREATE TABLE IF NOT EXISTS public.customers (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('CS', 8),
    customer_code VARCHAR(30) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(50),
    status INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_customers PRIMARY KEY (id)
);

-- TBL-users：ユーザ
CREATE TABLE IF NOT EXISTS public.users (
    user_id VARCHAR(10) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    role INT NOT NULL,
    status INT NOT NULL,
    customer_id VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT chk_users_customer_id CHECK ((role = 3) = (customer_id IS NOT NULL))
);

-- TBL-user-contacts：ユーザ連絡先
-- no soft delete: users の 1:1 / 1:0..1 子（その場で更新）
CREATE TABLE IF NOT EXISTS public.user_contacts (
    -- shared PK: same as users.user_id
    user_id VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_user_contacts PRIMARY KEY (user_id),
    CONSTRAINT uq_user_contacts_email UNIQUE (email)
);

-- TBL-system-locks：システムロック
-- no soft delete: users の 1:1 / 1:0..1 子（その場で更新）
CREATE TABLE IF NOT EXISTS public.system_locks (
    -- shared PK: same as users.user_id
    user_id VARCHAR(10) NOT NULL,
    lock_started_at TIMESTAMPTZ,
    lock_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_system_locks PRIMARY KEY (user_id)
);

-- TBL-customer-contacts：顧客連絡先
CREATE TABLE IF NOT EXISTS public.customer_contacts (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('CC', 8),
    customer_id VARCHAR(10) NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    is_notification_recipient BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_customer_contacts PRIMARY KEY (id)
);

-- TBL-customer-assignments：担当割当
CREATE TABLE IF NOT EXISTS public.customer_assignments (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AS', 8),
    customer_id VARCHAR(10) NOT NULL,
    user_id VARCHAR(10) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_customer_assignments PRIMARY KEY (id)
);

-- TBL-products：商品
CREATE TABLE IF NOT EXISTS public.products (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('PR', 8),
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_category VARCHAR(100),
    standard_unit_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_category INT NOT NULL,
    status INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT chk_products_std_unit_price CHECK (standard_unit_price >= 0)
);

-- TBL-tax-rates：税率
CREATE TABLE IF NOT EXISTS public.tax_rates (
    tax_category INT NOT NULL,
    tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_tax_rates PRIMARY KEY (tax_category),
    CONSTRAINT chk_tax_rates_tax_rate CHECK (tax_rate BETWEEN 0 AND 100)
);

-- TBL-orders：注文
CREATE TABLE IF NOT EXISTS public.orders (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('OD', 8),
    customer_id VARCHAR(10) NOT NULL,
    sales_user_id VARCHAR(10) NOT NULL,
    order_date DATE NOT NULL,
    status INT NOT NULL,
    subtotal_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    revised_from_order_id VARCHAR(10),
    cancel_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT chk_orders_cancel_reason CHECK (status <> 5 OR cancel_reason IS NOT NULL)
);

-- TBL-order-items：注文明細
CREATE TABLE IF NOT EXISTS public.order_items (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('OI', 8),
    order_id VARCHAR(10) NOT NULL,
    line_number NUMERIC NOT NULL DEFAULT 0,
    product_id VARCHAR(10) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(15,3) NOT NULL DEFAULT 0,
    unit_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_category INT NOT NULL,
    tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    line_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0)
);

-- TBL-order-status-histories：注文状態履歴
-- no soft delete: 履歴テーブル
CREATE TABLE IF NOT EXISTS public.order_status_histories (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('OH', 8),
    order_id VARCHAR(10) NOT NULL,
    from_status INT,
    to_status INT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_order_status_histories PRIMARY KEY (id)
);

-- TBL-approval-rules：承認ルール
CREATE TABLE IF NOT EXISTS public.approval_rules (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AR', 8),
    min_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    max_amount NUMERIC(15,2),
    approval_step NUMERIC NOT NULL DEFAULT 1,
    approver_role INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from_date DATE NOT NULL,
    effective_to_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_approval_rules PRIMARY KEY (id),
    CONSTRAINT chk_apr_rules_approver_role CHECK (approver_role = 1),
    CONSTRAINT chk_approval_rules_max_amount CHECK (max_amount IS NULL OR max_amount >= min_amount),
    CONSTRAINT chk_apr_rules_eff_to_date CHECK (effective_to_date IS NULL OR effective_to_date >= effective_from_date)
);

-- TBL-approval-requests：承認依頼
CREATE TABLE IF NOT EXISTS public.approval_requests (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AQ', 8),
    order_id VARCHAR(10) NOT NULL,
    approval_step NUMERIC NOT NULL DEFAULT 1,
    approver_user_id VARCHAR(10),
    rule_snapshot JSONB NOT NULL,
    status INT NOT NULL,
    acted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_approval_requests PRIMARY KEY (id)
);

-- TBL-approval-histories：承認履歴
-- no soft delete: 履歴テーブル
CREATE TABLE IF NOT EXISTS public.approval_histories (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AH', 8),
    approval_request_id VARCHAR(10) NOT NULL,
    approval_action INT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT pk_approval_histories PRIMARY KEY (id),
    CONSTRAINT chk_approval_histories_reason CHECK (approval_action <> 2 OR reason IS NOT NULL)
);

-- TBL-order-attachments：注文添付
CREATE TABLE IF NOT EXISTS public.order_attachments (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AT', 8),
    order_id VARCHAR(10) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(512) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size NUMERIC NOT NULL DEFAULT 0,
    visibility INT NOT NULL,
    scan_status INT NOT NULL,
    storage_purged_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_order_attachments PRIMARY KEY (id),
    CONSTRAINT chk_ord_atts_file_size CHECK (file_size > 0 AND file_size <= 10485760)
);

-- TBL-export-jobs：CSV 出力ジョブ
CREATE TABLE IF NOT EXISTS public.export_jobs (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('EJ', 8),
    filter_condition JSONB NOT NULL,
    status INT NOT NULL,
    storage_path VARCHAR(512),
    record_count NUMERIC NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ,
    error_message TEXT,
    completed_at TIMESTAMPTZ,
    storage_purged_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_export_jobs PRIMARY KEY (id)
);

-- TBL-notification-logs：通知ログ
CREATE TABLE IF NOT EXISTS public.notification_logs (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('NL', 8),
    notification_event INT NOT NULL,
    order_id VARCHAR(10),
    recipient_email VARCHAR(255) NOT NULL,
    status INT NOT NULL,
    export_job_id VARCHAR(10),
    attempt_count NUMERIC NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    error_message TEXT,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_notification_logs PRIMARY KEY (id)
);

-- TBL-audit-logs：監査ログ
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id VARCHAR(10) NOT NULL DEFAULT public.next_business_id('AL', 8),
    actor_user_id VARCHAR(10),
    audit_action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(100),
    trace_id VARCHAR(100),
    ip_address VARCHAR(45),
    audit_detail JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(10) NOT NULL DEFAULT 'SYSTEM',
    deleted_at TIMESTAMPTZ,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------
-- 外部キー
-- ---------------------------------------------------------------------
ALTER TABLE public.customers
    ADD CONSTRAINT fk_customers_cus_sts_codes FOREIGN KEY (status) REFERENCES public.customer_status_codes (customer_status_code);
ALTER TABLE public.users
    ADD CONSTRAINT fk_users_user_role_codes FOREIGN KEY (role) REFERENCES public.user_role_codes (user_role_code);
ALTER TABLE public.users
    ADD CONSTRAINT fk_users_user_status_codes FOREIGN KEY (status) REFERENCES public.user_status_codes (user_status_code);
ALTER TABLE public.users
    ADD CONSTRAINT fk_users_customers FOREIGN KEY (customer_id) REFERENCES public.customers (id);
ALTER TABLE public.user_contacts
    ADD CONSTRAINT fk_user_contacts_users FOREIGN KEY (user_id) REFERENCES public.users (user_id);
ALTER TABLE public.system_locks
    ADD CONSTRAINT fk_system_locks_users FOREIGN KEY (user_id) REFERENCES public.users (user_id);
ALTER TABLE public.customer_contacts
    ADD CONSTRAINT fk_customer_contacts_customers FOREIGN KEY (customer_id) REFERENCES public.customers (id);
ALTER TABLE public.customer_assignments
    ADD CONSTRAINT fk_cus_asg_customers FOREIGN KEY (customer_id) REFERENCES public.customers (id);
ALTER TABLE public.customer_assignments
    ADD CONSTRAINT fk_customer_assignments_users FOREIGN KEY (user_id) REFERENCES public.users (user_id);
ALTER TABLE public.products
    ADD CONSTRAINT fk_products_tax_category_codes FOREIGN KEY (tax_category) REFERENCES public.tax_category_codes (tax_category_code);
ALTER TABLE public.products
    ADD CONSTRAINT fk_products_prd_sts_codes FOREIGN KEY (status) REFERENCES public.product_status_codes (product_status_code);
ALTER TABLE public.tax_rates
    ADD CONSTRAINT fk_tax_rates_tax_cat_codes FOREIGN KEY (tax_category) REFERENCES public.tax_category_codes (tax_category_code);
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_customers FOREIGN KEY (customer_id) REFERENCES public.customers (id);
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_users FOREIGN KEY (sales_user_id) REFERENCES public.users (user_id);
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_order_status_codes FOREIGN KEY (status) REFERENCES public.order_status_codes (order_status_code);
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_orders FOREIGN KEY (revised_from_order_id) REFERENCES public.orders (id);
ALTER TABLE public.order_items
    ADD CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.order_items
    ADD CONSTRAINT fk_order_items_products FOREIGN KEY (product_id) REFERENCES public.products (id);
ALTER TABLE public.order_items
    ADD CONSTRAINT fk_order_items_tax_cat_codes FOREIGN KEY (tax_category) REFERENCES public.tax_category_codes (tax_category_code);
ALTER TABLE public.order_status_histories
    ADD CONSTRAINT fk_ord_sts_hist_orders FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.order_status_histories
    ADD CONSTRAINT fk_ord_sts_hist_from_status FOREIGN KEY (from_status) REFERENCES public.order_status_codes (order_status_code);
ALTER TABLE public.order_status_histories
    ADD CONSTRAINT fk_ord_sts_hist_to_status FOREIGN KEY (to_status) REFERENCES public.order_status_codes (order_status_code);
ALTER TABLE public.approval_rules
    ADD CONSTRAINT fk_apr_rules_user_role_codes FOREIGN KEY (approver_role) REFERENCES public.user_role_codes (user_role_code);
ALTER TABLE public.approval_requests
    ADD CONSTRAINT fk_approval_requests_orders FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.approval_requests
    ADD CONSTRAINT fk_approval_requests_users FOREIGN KEY (approver_user_id) REFERENCES public.users (user_id);
ALTER TABLE public.approval_requests
    ADD CONSTRAINT fk_apr_req_apr_req_sts_codes FOREIGN KEY (status) REFERENCES public.approval_request_status_codes (approval_request_status_code);
ALTER TABLE public.approval_histories
    ADD CONSTRAINT fk_apr_hist_apr_requests FOREIGN KEY (approval_request_id) REFERENCES public.approval_requests (id);
ALTER TABLE public.approval_histories
    ADD CONSTRAINT fk_apr_hist_apr_action_codes FOREIGN KEY (approval_action) REFERENCES public.approval_action_codes (approval_action_code);
ALTER TABLE public.order_attachments
    ADD CONSTRAINT fk_order_attachments_orders FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.order_attachments
    ADD CONSTRAINT fk_ord_atts_att_vis_codes FOREIGN KEY (visibility) REFERENCES public.attachment_visibility_codes (attachment_visibility_code);
ALTER TABLE public.order_attachments
    ADD CONSTRAINT fk_ord_atts_scan_sts_codes FOREIGN KEY (scan_status) REFERENCES public.scan_status_codes (scan_status_code);
ALTER TABLE public.export_jobs
    ADD CONSTRAINT fk_export_jobs_exp_sts_codes FOREIGN KEY (status) REFERENCES public.export_status_codes (export_status_code);
ALTER TABLE public.notification_logs
    ADD CONSTRAINT fk_ntf_logs_ntf_evt_codes FOREIGN KEY (notification_event) REFERENCES public.notification_event_codes (notification_event_code);
ALTER TABLE public.notification_logs
    ADD CONSTRAINT fk_notification_logs_orders FOREIGN KEY (order_id) REFERENCES public.orders (id);
ALTER TABLE public.notification_logs
    ADD CONSTRAINT fk_ntf_logs_export_jobs FOREIGN KEY (export_job_id) REFERENCES public.export_jobs (id);
ALTER TABLE public.notification_logs
    ADD CONSTRAINT fk_ntf_logs_ntf_sts_codes FOREIGN KEY (status) REFERENCES public.notification_status_codes (notification_status_code);
ALTER TABLE public.audit_logs
    ADD CONSTRAINT fk_audit_logs_users FOREIGN KEY (actor_user_id) REFERENCES public.users (user_id);

-- ---------------------------------------------------------------------
-- インデックス（主キー・ユニーク制約はテーブル定義で作成済み）
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS uq_customers_customer_code ON public.customers (customer_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_customers_status ON public.customers (status);
CREATE INDEX IF NOT EXISTS idx_users_customer_id ON public.users (customer_id);
CREATE INDEX IF NOT EXISTS idx_users_role_status ON public.users (role, status);
CREATE INDEX IF NOT EXISTS idx_users_status ON public.users (status);
CREATE INDEX IF NOT EXISTS idx_cus_cnt_cus_id_is_active ON public.customer_contacts (customer_id, is_active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_cus_asg_cus_id_user_id ON public.customer_assignments (customer_id, user_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_cus_asg_user_id_is_active ON public.customer_assignments (user_id, is_active);
CREATE UNIQUE INDEX IF NOT EXISTS uq_products_product_code ON public.products (product_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_products_tax_category ON public.products (tax_category);
CREATE INDEX IF NOT EXISTS idx_products_status ON public.products (status);
CREATE INDEX IF NOT EXISTS idx_orders_cus_id_order_date ON public.orders (customer_id, order_date);
CREATE INDEX IF NOT EXISTS idx_orders_sales_user_status ON public.orders (sales_user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_order_date ON public.orders (status, order_date);
CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_revised_from ON public.orders (revised_from_order_id) WHERE revised_from_order_id IS NOT NULL AND deleted_at IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_order_items_order_line_no ON public.order_items (order_id, line_number) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON public.order_items (product_id);
CREATE INDEX IF NOT EXISTS idx_order_items_tax_category ON public.order_items (tax_category);
CREATE INDEX IF NOT EXISTS idx_ord_sts_hist_order_created ON public.order_status_histories (order_id, created_at);
CREATE INDEX IF NOT EXISTS idx_ord_sts_hist_from_status ON public.order_status_histories (from_status);
CREATE INDEX IF NOT EXISTS idx_ord_sts_hist_to_status ON public.order_status_histories (to_status);
CREATE INDEX IF NOT EXISTS idx_apr_rules_active_eff_from ON public.approval_rules (is_active, effective_from_date);
CREATE INDEX IF NOT EXISTS idx_apr_rules_approver_role ON public.approval_rules (approver_role);
CREATE INDEX IF NOT EXISTS idx_apr_req_order_id_status ON public.approval_requests (order_id, status);
CREATE INDEX IF NOT EXISTS idx_apr_req_approver_status ON public.approval_requests (approver_user_id, status);
CREATE INDEX IF NOT EXISTS idx_approval_requests_status ON public.approval_requests (status);
CREATE INDEX IF NOT EXISTS idx_apr_hist_apr_request_id ON public.approval_histories (approval_request_id);
CREATE INDEX IF NOT EXISTS idx_apr_hist_apr_action ON public.approval_histories (approval_action);
CREATE UNIQUE INDEX IF NOT EXISTS uq_ord_atts_storage_path ON public.order_attachments (storage_path) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_ord_atts_order_vis_scan ON public.order_attachments (order_id, visibility, scan_status);
CREATE INDEX IF NOT EXISTS idx_ord_atts_visibility ON public.order_attachments (visibility);
CREATE INDEX IF NOT EXISTS idx_ord_atts_scan_status ON public.order_attachments (scan_status);
CREATE INDEX IF NOT EXISTS idx_export_jobs_creator_status ON public.export_jobs (created_by, status);
CREATE INDEX IF NOT EXISTS idx_export_jobs_status ON public.export_jobs (status);
CREATE INDEX IF NOT EXISTS idx_ntf_logs_status_created ON public.notification_logs (status, created_at);
CREATE INDEX IF NOT EXISTS idx_ntf_logs_status_next_try ON public.notification_logs (status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_notification_logs_order_id ON public.notification_logs (order_id);
CREATE INDEX IF NOT EXISTS idx_ntf_logs_export_job_id ON public.notification_logs (export_job_id);
CREATE INDEX IF NOT EXISTS idx_ntf_logs_ntf_event ON public.notification_logs (notification_event);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_created ON public.audit_logs (actor_user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_tgt_type_id ON public.audit_logs (target_type, target_id);

-- ---------------------------------------------------------------------
-- updated_at トリガー（spec-database-rule.md §4.3-Trigger。全テーブル）
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_set_updated_at_user_role_codes
BEFORE UPDATE ON public.user_role_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_user_status_codes
BEFORE UPDATE ON public.user_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_customer_status_codes
BEFORE UPDATE ON public.customer_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_product_status_codes
BEFORE UPDATE ON public.product_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_tax_category_codes
BEFORE UPDATE ON public.tax_category_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_order_status_codes
BEFORE UPDATE ON public.order_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_approval_request_status_codes
BEFORE UPDATE ON public.approval_request_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_approval_action_codes
BEFORE UPDATE ON public.approval_action_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_attachment_visibility_codes
BEFORE UPDATE ON public.attachment_visibility_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_scan_status_codes
BEFORE UPDATE ON public.scan_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_notification_event_codes
BEFORE UPDATE ON public.notification_event_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_notification_status_codes
BEFORE UPDATE ON public.notification_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_export_status_codes
BEFORE UPDATE ON public.export_status_codes
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_customers
BEFORE UPDATE ON public.customers
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_users
BEFORE UPDATE ON public.users
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_user_contacts
BEFORE UPDATE ON public.user_contacts
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_system_locks
BEFORE UPDATE ON public.system_locks
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_customer_contacts
BEFORE UPDATE ON public.customer_contacts
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_customer_assignments
BEFORE UPDATE ON public.customer_assignments
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_products
BEFORE UPDATE ON public.products
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_tax_rates
BEFORE UPDATE ON public.tax_rates
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_orders
BEFORE UPDATE ON public.orders
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_order_items
BEFORE UPDATE ON public.order_items
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_order_status_histories
BEFORE UPDATE ON public.order_status_histories
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_approval_rules
BEFORE UPDATE ON public.approval_rules
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_approval_requests
BEFORE UPDATE ON public.approval_requests
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_approval_histories
BEFORE UPDATE ON public.approval_histories
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_order_attachments
BEFORE UPDATE ON public.order_attachments
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_export_jobs
BEFORE UPDATE ON public.export_jobs
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_notification_logs
BEFORE UPDATE ON public.notification_logs
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_set_updated_at_audit_logs
BEFORE UPDATE ON public.audit_logs
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ---------------------------------------------------------------------
-- コメント（論理名）
-- ---------------------------------------------------------------------
COMMENT ON TABLE public.user_role_codes IS 'ロールコード：ロール列挙（T07-1）のコード表';
COMMENT ON COLUMN public.user_role_codes.user_role_code IS 'ロールコード';
COMMENT ON COLUMN public.user_role_codes.user_role_name IS 'ロール名';
COMMENT ON COLUMN public.user_role_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.user_role_codes.created_by IS '作成者';
COMMENT ON COLUMN public.user_role_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.user_role_codes.updated_by IS '更新者';
COMMENT ON TABLE public.user_status_codes IS '利用者状態コード：利用者状態列挙（T07-2）のコード表';
COMMENT ON COLUMN public.user_status_codes.user_status_code IS '利用者状態コード';
COMMENT ON COLUMN public.user_status_codes.user_status_name IS '利用者状態名';
COMMENT ON COLUMN public.user_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.user_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.user_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.user_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.customer_status_codes IS '顧客状態コード：顧客状態列挙（T07-3）のコード表';
COMMENT ON COLUMN public.customer_status_codes.customer_status_code IS '顧客状態コード';
COMMENT ON COLUMN public.customer_status_codes.customer_status_name IS '顧客状態名';
COMMENT ON COLUMN public.customer_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.customer_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.customer_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.customer_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.product_status_codes IS '商品状態コード：商品状態列挙（T07-4）のコード表';
COMMENT ON COLUMN public.product_status_codes.product_status_code IS '商品状態コード';
COMMENT ON COLUMN public.product_status_codes.product_status_name IS '商品状態名';
COMMENT ON COLUMN public.product_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.product_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.product_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.product_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.tax_category_codes IS '税区分コード：税区分列挙（T07-5）のコード表';
COMMENT ON COLUMN public.tax_category_codes.tax_category_code IS '税区分コード';
COMMENT ON COLUMN public.tax_category_codes.tax_category_name IS '税区分名';
COMMENT ON COLUMN public.tax_category_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.tax_category_codes.created_by IS '作成者';
COMMENT ON COLUMN public.tax_category_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.tax_category_codes.updated_by IS '更新者';
COMMENT ON TABLE public.order_status_codes IS '注文状態コード：注文状態列挙（T07-6）のコード表';
COMMENT ON COLUMN public.order_status_codes.order_status_code IS '注文状態コード';
COMMENT ON COLUMN public.order_status_codes.order_status_name IS '注文状態名';
COMMENT ON COLUMN public.order_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.order_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.order_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.order_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.approval_request_status_codes IS '承認依頼状態コード：承認依頼状態列挙（T07-7）のコード表';
COMMENT ON COLUMN public.approval_request_status_codes.approval_request_status_code IS '承認依頼状態コード';
COMMENT ON COLUMN public.approval_request_status_codes.approval_request_status_name IS '承認依頼状態名';
COMMENT ON COLUMN public.approval_request_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.approval_request_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.approval_request_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.approval_request_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.approval_action_codes IS '承認操作コード：承認操作列挙（T07-8）のコード表';
COMMENT ON COLUMN public.approval_action_codes.approval_action_code IS '承認操作コード';
COMMENT ON COLUMN public.approval_action_codes.approval_action_name IS '承認操作名';
COMMENT ON COLUMN public.approval_action_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.approval_action_codes.created_by IS '作成者';
COMMENT ON COLUMN public.approval_action_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.approval_action_codes.updated_by IS '更新者';
COMMENT ON TABLE public.attachment_visibility_codes IS '添付公開範囲コード：添付公開範囲列挙（T07-9）のコード表';
COMMENT ON COLUMN public.attachment_visibility_codes.attachment_visibility_code IS '添付公開範囲コード';
COMMENT ON COLUMN public.attachment_visibility_codes.attachment_visibility_name IS '添付公開範囲名';
COMMENT ON COLUMN public.attachment_visibility_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.attachment_visibility_codes.created_by IS '作成者';
COMMENT ON COLUMN public.attachment_visibility_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.attachment_visibility_codes.updated_by IS '更新者';
COMMENT ON TABLE public.scan_status_codes IS 'スキャン状態コード：スキャン状態列挙（T07-10）のコード表';
COMMENT ON COLUMN public.scan_status_codes.scan_status_code IS 'スキャン状態コード';
COMMENT ON COLUMN public.scan_status_codes.scan_status_name IS 'スキャン状態名';
COMMENT ON COLUMN public.scan_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.scan_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.scan_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.scan_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.notification_event_codes IS '通知イベントコード：通知イベント列挙（T07-11）のコード表';
COMMENT ON COLUMN public.notification_event_codes.notification_event_code IS '通知イベントコード';
COMMENT ON COLUMN public.notification_event_codes.notification_event_name IS '通知イベント名';
COMMENT ON COLUMN public.notification_event_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.notification_event_codes.created_by IS '作成者';
COMMENT ON COLUMN public.notification_event_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.notification_event_codes.updated_by IS '更新者';
COMMENT ON TABLE public.notification_status_codes IS '通知状態コード：通知状態列挙（T07-12）のコード表';
COMMENT ON COLUMN public.notification_status_codes.notification_status_code IS '通知状態コード';
COMMENT ON COLUMN public.notification_status_codes.notification_status_name IS '通知状態名';
COMMENT ON COLUMN public.notification_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.notification_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.notification_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.notification_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.export_status_codes IS '出力状態コード：出力状態列挙（T07-13）のコード表';
COMMENT ON COLUMN public.export_status_codes.export_status_code IS '出力状態コード';
COMMENT ON COLUMN public.export_status_codes.export_status_name IS '出力状態名';
COMMENT ON COLUMN public.export_status_codes.created_at IS '作成日時';
COMMENT ON COLUMN public.export_status_codes.created_by IS '作成者';
COMMENT ON COLUMN public.export_status_codes.updated_at IS '更新日時';
COMMENT ON COLUMN public.export_status_codes.updated_by IS '更新者';
COMMENT ON TABLE public.customers IS '顧客：顧客マスタ';
COMMENT ON COLUMN public.customers.id IS '顧客ID';
COMMENT ON COLUMN public.customers.customer_code IS '顧客コード';
COMMENT ON COLUMN public.customers.customer_name IS '顧客名';
COMMENT ON COLUMN public.customers.email IS 'メールアドレス';
COMMENT ON COLUMN public.customers.phone_number IS '電話番号';
COMMENT ON COLUMN public.customers.status IS '状態';
COMMENT ON COLUMN public.customers.created_at IS '作成日時';
COMMENT ON COLUMN public.customers.created_by IS '作成者';
COMMENT ON COLUMN public.customers.updated_at IS '更新日時';
COMMENT ON COLUMN public.customers.updated_by IS '更新者';
COMMENT ON COLUMN public.customers.deleted_at IS '削除日時';
COMMENT ON TABLE public.users IS 'ユーザ：利用者。パスワードは保持しない（Magic Link 認証）';
COMMENT ON COLUMN public.users.user_id IS 'ユーザID';
COMMENT ON COLUMN public.users.user_name IS '表示名';
COMMENT ON COLUMN public.users.role IS 'ロール';
COMMENT ON COLUMN public.users.status IS '状態';
COMMENT ON COLUMN public.users.customer_id IS '顧客ID';
COMMENT ON COLUMN public.users.created_at IS '作成日時';
COMMENT ON COLUMN public.users.created_by IS '作成者';
COMMENT ON COLUMN public.users.updated_at IS '更新日時';
COMMENT ON COLUMN public.users.updated_by IS '更新者';
COMMENT ON COLUMN public.users.deleted_at IS '削除日時';
COMMENT ON TABLE public.user_contacts IS 'ユーザ連絡先：Magic Link・通知の送信先メール（users と 1:1）。users と同時に作成し、行は更新のみで削除しない';
COMMENT ON COLUMN public.user_contacts.user_id IS 'ユーザID';
COMMENT ON COLUMN public.user_contacts.email IS 'メールアドレス';
COMMENT ON COLUMN public.user_contacts.created_at IS '作成日時';
COMMENT ON COLUMN public.user_contacts.created_by IS '作成者';
COMMENT ON COLUMN public.user_contacts.updated_at IS '更新日時';
COMMENT ON COLUMN public.user_contacts.updated_by IS '更新者';
COMMENT ON TABLE public.system_locks IS 'システムロック：ADMIN による利用者ロック（users と 1:0..1）。ログイン可否判定に使用。解除は lock_started_at を NULL に更新し、行は削除しない';
COMMENT ON COLUMN public.system_locks.user_id IS 'ユーザID';
COMMENT ON COLUMN public.system_locks.lock_started_at IS 'ロック開始日時';
COMMENT ON COLUMN public.system_locks.lock_reason IS 'ロック理由';
COMMENT ON COLUMN public.system_locks.created_at IS '作成日時';
COMMENT ON COLUMN public.system_locks.created_by IS '作成者';
COMMENT ON COLUMN public.system_locks.updated_at IS '更新日時';
COMMENT ON COLUMN public.system_locks.updated_by IS '更新者';
COMMENT ON TABLE public.customer_contacts IS '顧客連絡先：顧客の連絡先・通知先';
COMMENT ON COLUMN public.customer_contacts.id IS '顧客連絡先ID';
COMMENT ON COLUMN public.customer_contacts.customer_id IS '顧客ID';
COMMENT ON COLUMN public.customer_contacts.contact_name IS '連絡先名';
COMMENT ON COLUMN public.customer_contacts.email IS 'メールアドレス';
COMMENT ON COLUMN public.customer_contacts.is_notification_recipient IS '通知先フラグ';
COMMENT ON COLUMN public.customer_contacts.is_active IS '有効フラグ';
COMMENT ON COLUMN public.customer_contacts.created_at IS '作成日時';
COMMENT ON COLUMN public.customer_contacts.created_by IS '作成者';
COMMENT ON COLUMN public.customer_contacts.updated_at IS '更新日時';
COMMENT ON COLUMN public.customer_contacts.updated_by IS '更新者';
COMMENT ON COLUMN public.customer_contacts.deleted_at IS '削除日時';
COMMENT ON TABLE public.customer_assignments IS '担当割当：EMPLOYEE と担当顧客の割当（属性を持つため業務エンティティとして扱う）';
COMMENT ON COLUMN public.customer_assignments.id IS '担当割当ID';
COMMENT ON COLUMN public.customer_assignments.customer_id IS '顧客ID';
COMMENT ON COLUMN public.customer_assignments.user_id IS 'ユーザID';
COMMENT ON COLUMN public.customer_assignments.is_active IS '有効フラグ';
COMMENT ON COLUMN public.customer_assignments.assigned_date IS '割当日';
COMMENT ON COLUMN public.customer_assignments.created_at IS '作成日時';
COMMENT ON COLUMN public.customer_assignments.created_by IS '作成者';
COMMENT ON COLUMN public.customer_assignments.updated_at IS '更新日時';
COMMENT ON COLUMN public.customer_assignments.updated_by IS '更新者';
COMMENT ON COLUMN public.customer_assignments.deleted_at IS '削除日時';
COMMENT ON TABLE public.products IS '商品：商品マスタ';
COMMENT ON COLUMN public.products.id IS '商品ID';
COMMENT ON COLUMN public.products.product_code IS '商品コード';
COMMENT ON COLUMN public.products.product_name IS '商品名';
COMMENT ON COLUMN public.products.product_category IS '商品カテゴリ';
COMMENT ON COLUMN public.products.standard_unit_price IS '標準単価';
COMMENT ON COLUMN public.products.tax_category IS '税区分';
COMMENT ON COLUMN public.products.status IS '状態';
COMMENT ON COLUMN public.products.created_at IS '作成日時';
COMMENT ON COLUMN public.products.created_by IS '作成者';
COMMENT ON COLUMN public.products.updated_at IS '更新日時';
COMMENT ON COLUMN public.products.updated_by IS '更新者';
COMMENT ON COLUMN public.products.deleted_at IS '削除日時';
COMMENT ON TABLE public.tax_rates IS '税率：税区分ごとの税率（§4.1-NATURAL）。注文時に明細へ複写する';
COMMENT ON COLUMN public.tax_rates.tax_category IS '税区分';
COMMENT ON COLUMN public.tax_rates.tax_rate IS '税率';
COMMENT ON COLUMN public.tax_rates.created_at IS '作成日時';
COMMENT ON COLUMN public.tax_rates.created_by IS '作成者';
COMMENT ON COLUMN public.tax_rates.updated_at IS '更新日時';
COMMENT ON COLUMN public.tax_rates.updated_by IS '更新者';
COMMENT ON COLUMN public.tax_rates.deleted_at IS '削除日時';
COMMENT ON TABLE public.orders IS '注文：注文ヘッダ。注文番号は id とする';
COMMENT ON COLUMN public.orders.id IS '注文ID（注文番号）';
COMMENT ON COLUMN public.orders.customer_id IS '顧客ID';
COMMENT ON COLUMN public.orders.sales_user_id IS '担当者ユーザID';
COMMENT ON COLUMN public.orders.order_date IS '注文日';
COMMENT ON COLUMN public.orders.status IS '状態';
COMMENT ON COLUMN public.orders.subtotal_amount IS '小計';
COMMENT ON COLUMN public.orders.tax_amount IS '税額';
COMMENT ON COLUMN public.orders.total_amount IS '合計';
COMMENT ON COLUMN public.orders.revised_from_order_id IS '訂正元注文ID';
COMMENT ON COLUMN public.orders.cancel_reason IS '取消し理由';
COMMENT ON COLUMN public.orders.created_at IS '作成日時';
COMMENT ON COLUMN public.orders.created_by IS '作成者';
COMMENT ON COLUMN public.orders.updated_at IS '更新日時';
COMMENT ON COLUMN public.orders.updated_by IS '更新者';
COMMENT ON COLUMN public.orders.deleted_at IS '削除日時';
COMMENT ON TABLE public.order_items IS '注文明細：注文明細。商品・税率は注文時点で複写する';
COMMENT ON COLUMN public.order_items.id IS '注文明細ID';
COMMENT ON COLUMN public.order_items.order_id IS '注文ID';
COMMENT ON COLUMN public.order_items.line_number IS '行番号';
COMMENT ON COLUMN public.order_items.product_id IS '商品ID';
COMMENT ON COLUMN public.order_items.product_code IS '商品コード（複写）';
COMMENT ON COLUMN public.order_items.product_name IS '商品名（複写）';
COMMENT ON COLUMN public.order_items.quantity IS '数量';
COMMENT ON COLUMN public.order_items.unit_price IS '単価';
COMMENT ON COLUMN public.order_items.tax_category IS '税区分（複写）';
COMMENT ON COLUMN public.order_items.tax_rate IS '税率（複写）';
COMMENT ON COLUMN public.order_items.line_amount IS '明細金額';
COMMENT ON COLUMN public.order_items.tax_amount IS '明細税額';
COMMENT ON COLUMN public.order_items.created_at IS '作成日時';
COMMENT ON COLUMN public.order_items.created_by IS '作成者';
COMMENT ON COLUMN public.order_items.updated_at IS '更新日時';
COMMENT ON COLUMN public.order_items.updated_by IS '更新者';
COMMENT ON COLUMN public.order_items.deleted_at IS '削除日時';
COMMENT ON TABLE public.order_status_histories IS '注文状態履歴：注文状態の遷移履歴（追加専用）。変更者・日時は created_by / created_at';
COMMENT ON COLUMN public.order_status_histories.id IS '注文状態履歴ID';
COMMENT ON COLUMN public.order_status_histories.order_id IS '注文ID';
COMMENT ON COLUMN public.order_status_histories.from_status IS '変更前状態';
COMMENT ON COLUMN public.order_status_histories.to_status IS '変更後状態';
COMMENT ON COLUMN public.order_status_histories.reason IS '理由';
COMMENT ON COLUMN public.order_status_histories.created_at IS '作成日時';
COMMENT ON COLUMN public.order_status_histories.created_by IS '作成者';
COMMENT ON COLUMN public.order_status_histories.updated_at IS '更新日時';
COMMENT ON COLUMN public.order_status_histories.updated_by IS '更新者';
COMMENT ON TABLE public.approval_rules IS '承認ルール：承認経路。初版は 0〜上限なし・1 段・ADMIN の 1 行のみ（BR-APR-001）。承認者は特定しない（有効な ADMIN のうち申請者以外）';
COMMENT ON COLUMN public.approval_rules.id IS '承認ルールID';
COMMENT ON COLUMN public.approval_rules.min_amount IS '金額下限';
COMMENT ON COLUMN public.approval_rules.max_amount IS '金額上限';
COMMENT ON COLUMN public.approval_rules.approval_step IS '承認段階';
COMMENT ON COLUMN public.approval_rules.approver_role IS '承認者ロール';
COMMENT ON COLUMN public.approval_rules.is_active IS '有効フラグ';
COMMENT ON COLUMN public.approval_rules.effective_from_date IS '適用開始日';
COMMENT ON COLUMN public.approval_rules.effective_to_date IS '適用終了日';
COMMENT ON COLUMN public.approval_rules.created_at IS '作成日時';
COMMENT ON COLUMN public.approval_rules.created_by IS '作成者';
COMMENT ON COLUMN public.approval_rules.updated_at IS '更新日時';
COMMENT ON COLUMN public.approval_rules.updated_by IS '更新者';
COMMENT ON COLUMN public.approval_rules.deleted_at IS '削除日時';
COMMENT ON TABLE public.approval_requests IS '承認依頼：注文ごとの承認依頼と適用ルールのスナップショット。再申請ごとに新しい行を作る';
COMMENT ON COLUMN public.approval_requests.id IS '承認依頼ID';
COMMENT ON COLUMN public.approval_requests.order_id IS '注文ID';
COMMENT ON COLUMN public.approval_requests.approval_step IS '承認段階';
COMMENT ON COLUMN public.approval_requests.approver_user_id IS '承認者ユーザID';
COMMENT ON COLUMN public.approval_requests.rule_snapshot IS 'ルールスナップショット';
COMMENT ON COLUMN public.approval_requests.status IS '状態';
COMMENT ON COLUMN public.approval_requests.acted_at IS '処理日時';
COMMENT ON COLUMN public.approval_requests.created_at IS '作成日時';
COMMENT ON COLUMN public.approval_requests.created_by IS '作成者';
COMMENT ON COLUMN public.approval_requests.updated_at IS '更新日時';
COMMENT ON COLUMN public.approval_requests.updated_by IS '更新者';
COMMENT ON COLUMN public.approval_requests.deleted_at IS '削除日時';
COMMENT ON TABLE public.approval_histories IS '承認履歴：承認・差戻しの操作履歴（追加専用）。操作者・日時は created_by / created_at';
COMMENT ON COLUMN public.approval_histories.id IS '承認履歴ID';
COMMENT ON COLUMN public.approval_histories.approval_request_id IS '承認依頼ID';
COMMENT ON COLUMN public.approval_histories.approval_action IS '操作';
COMMENT ON COLUMN public.approval_histories.reason IS '理由';
COMMENT ON COLUMN public.approval_histories.created_at IS '作成日時';
COMMENT ON COLUMN public.approval_histories.created_by IS '作成者';
COMMENT ON COLUMN public.approval_histories.updated_at IS '更新日時';
COMMENT ON COLUMN public.approval_histories.updated_by IS '更新者';
COMMENT ON TABLE public.order_attachments IS '注文添付：注文添付のメタデータ。ファイル本体はオブジェクトストレージ';
COMMENT ON COLUMN public.order_attachments.id IS '添付ID';
COMMENT ON COLUMN public.order_attachments.order_id IS '注文ID';
COMMENT ON COLUMN public.order_attachments.original_file_name IS '元ファイル名';
COMMENT ON COLUMN public.order_attachments.storage_path IS '保存パス';
COMMENT ON COLUMN public.order_attachments.content_type IS 'コンテンツタイプ';
COMMENT ON COLUMN public.order_attachments.file_size IS 'ファイルサイズ';
COMMENT ON COLUMN public.order_attachments.visibility IS '公開範囲';
COMMENT ON COLUMN public.order_attachments.scan_status IS 'スキャン状態';
COMMENT ON COLUMN public.order_attachments.storage_purged_at IS 'ファイル削除日時';
COMMENT ON COLUMN public.order_attachments.created_at IS '作成日時';
COMMENT ON COLUMN public.order_attachments.created_by IS '作成者';
COMMENT ON COLUMN public.order_attachments.updated_at IS '更新日時';
COMMENT ON COLUMN public.order_attachments.updated_by IS '更新者';
COMMENT ON COLUMN public.order_attachments.deleted_at IS '削除日時';
COMMENT ON TABLE public.export_jobs IS 'CSV 出力ジョブ：CSV 出力ジョブ（全件を記録。10,000 件以下は同期生成して READY、10,001 件以上は QUEUED で非同期生成。BR-RPT-002）。依頼者は created_by';
COMMENT ON COLUMN public.export_jobs.id IS '出力ジョブID';
COMMENT ON COLUMN public.export_jobs.filter_condition IS '抽出条件';
COMMENT ON COLUMN public.export_jobs.status IS '状態';
COMMENT ON COLUMN public.export_jobs.storage_path IS '保存パス';
COMMENT ON COLUMN public.export_jobs.record_count IS '件数';
COMMENT ON COLUMN public.export_jobs.expires_at IS '有効期限';
COMMENT ON COLUMN public.export_jobs.error_message IS 'エラー内容';
COMMENT ON COLUMN public.export_jobs.completed_at IS '完了日時';
COMMENT ON COLUMN public.export_jobs.storage_purged_at IS 'ファイル削除日時';
COMMENT ON COLUMN public.export_jobs.created_at IS '作成日時';
COMMENT ON COLUMN public.export_jobs.created_by IS '作成者';
COMMENT ON COLUMN public.export_jobs.updated_at IS '更新日時';
COMMENT ON COLUMN public.export_jobs.updated_by IS '更新者';
COMMENT ON COLUMN public.export_jobs.deleted_at IS '削除日時';
COMMENT ON TABLE public.notification_logs IS '通知ログ：業務メール通知のアウトボックスと送信履歴（Magic Link は auth-service が送信し、ここには記録しない）';
COMMENT ON COLUMN public.notification_logs.id IS '通知ID';
COMMENT ON COLUMN public.notification_logs.notification_event IS '通知イベント';
COMMENT ON COLUMN public.notification_logs.order_id IS '注文ID';
COMMENT ON COLUMN public.notification_logs.recipient_email IS '宛先メール';
COMMENT ON COLUMN public.notification_logs.status IS '状態';
COMMENT ON COLUMN public.notification_logs.export_job_id IS '出力ジョブID';
COMMENT ON COLUMN public.notification_logs.attempt_count IS '試行回数';
COMMENT ON COLUMN public.notification_logs.next_attempt_at IS '次回送信日時';
COMMENT ON COLUMN public.notification_logs.error_message IS 'エラー内容';
COMMENT ON COLUMN public.notification_logs.sent_at IS '送信日時';
COMMENT ON COLUMN public.notification_logs.created_at IS '作成日時';
COMMENT ON COLUMN public.notification_logs.created_by IS '作成者';
COMMENT ON COLUMN public.notification_logs.updated_at IS '更新日時';
COMMENT ON COLUMN public.notification_logs.updated_by IS '更新者';
COMMENT ON COLUMN public.notification_logs.deleted_at IS '削除日時';
COMMENT ON TABLE public.audit_logs IS '監査ログ：重要操作の監査ログ（追加専用。論理削除は運用上行わない）';
COMMENT ON COLUMN public.audit_logs.id IS '監査ログID';
COMMENT ON COLUMN public.audit_logs.actor_user_id IS '操作者ユーザID';
COMMENT ON COLUMN public.audit_logs.audit_action IS '操作';
COMMENT ON COLUMN public.audit_logs.target_type IS '対象種別';
COMMENT ON COLUMN public.audit_logs.target_id IS '対象ID';
COMMENT ON COLUMN public.audit_logs.trace_id IS 'トレースID';
COMMENT ON COLUMN public.audit_logs.ip_address IS 'IP アドレス';
COMMENT ON COLUMN public.audit_logs.audit_detail IS '監査詳細';
COMMENT ON COLUMN public.audit_logs.created_at IS '作成日時';
COMMENT ON COLUMN public.audit_logs.created_by IS '作成者';
COMMENT ON COLUMN public.audit_logs.updated_at IS '更新日時';
COMMENT ON COLUMN public.audit_logs.updated_by IS '更新者';
COMMENT ON COLUMN public.audit_logs.deleted_at IS '削除日時';

-- 21_seed_sample.sql：開発用サンプルデータ（機構A。ローカル開発専用。本番・Flyway には含めない）。
-- spec-17 §3（TBD-MIG-03）：顧客・商品・EMPLOYEE / CUSTOMER 利用者・下書き注文。
-- 初期管理者 A0001 と承認ルール（INI-03〜05）は Flyway V1 が投入するため、ここでは作らない。
-- docker/.env の SEED_SAMPLE_DATA=false で投入を省略できる。メールは予約ドメイン example.com のダミー。
-- 固定 ID で投入するため、最後に採番シーケンスを進める（next_business_id が重複しないようにする）。

-- 顧客
INSERT INTO public.customers (id, customer_code, customer_name, email, phone_number, status) VALUES
    ('CS00000001', 'CUS-0001', '株式会社サンプル商事', 'info@sample-shoji.example.com', '03-0000-0001', 1),
    ('CS00000002', 'CUS-0002', 'テスト工業株式会社', 'info@test-kogyo.example.com', '03-0000-0002', 1),
    ('CS00000003', 'CUS-0003', 'デモ物産株式会社（取引停止）', NULL, NULL, 2);

INSERT INTO public.customer_contacts (id, customer_id, contact_name, email, is_notification_recipient, is_active) VALUES
    ('CC00000001', 'CS00000001', '鈴木 一郎', 'suzuki@sample-shoji.example.com', TRUE, TRUE),
    ('CC00000002', 'CS00000001', '高橋 美咲', 'takahashi@sample-shoji.example.com', FALSE, TRUE),
    ('CC00000003', 'CS00000002', '田中 健', 'tanaka@test-kogyo.example.com', TRUE, TRUE);

-- 利用者（EMPLOYEE 2 名、CUSTOMER 2 名。ADMIN は V1 の A0001）
INSERT INTO public.users (user_id, user_name, role, status, customer_id) VALUES
    ('E0001', '山田 花子', 2, 1, NULL),
    ('E0002', '佐藤 次郎', 2, 1, NULL),
    ('C0001', '鈴木 一郎', 3, 1, 'CS00000001'),
    ('C0002', '田中 健', 3, 1, 'CS00000002');

INSERT INTO public.user_contacts (user_id, email) VALUES
    ('E0001', 'yamada.hanako@example.com'),
    ('E0002', 'sato.jiro@example.com'),
    ('C0001', 'suzuki@sample-shoji.example.com'),
    ('C0002', 'tanaka@test-kogyo.example.com');

-- 担当割当
INSERT INTO public.customer_assignments (id, customer_id, user_id, is_active, assigned_date) VALUES
    ('AS00000001', 'CS00000001', 'E0001', TRUE, '2026-10-01'),
    ('AS00000002', 'CS00000002', 'E0002', TRUE, '2026-10-01'),
    ('AS00000003', 'CS00000003', 'E0002', TRUE, '2026-10-01');

-- 商品（税区分 1=STANDARD / 2=REDUCED / 3=EXEMPT、状態 2=INACTIVE の商品を 1 件含む）
INSERT INTO public.products (id, product_code, product_name, product_category, standard_unit_price, tax_category, status) VALUES
    ('PR00000001', 'P-1001', 'ボールペン（黒）', '文具', 1000.00, 1, 1),
    ('PR00000002', 'F-2001', 'ミネラルウォーター 500ml', '飲料', 500.00, 2, 1),
    ('PR00000003', 'B-3001', '業務マニュアル（書籍）', '書籍', 3000.00, 3, 1),
    ('PR00000004', 'P-9001', '旧型ノート（販売終了）', '文具', 300.00, 1, 2);

-- 下書き注文 2 件（明細の金額・税額は BR-ORD-004 の計算：税額は明細ごとに端数処理）
INSERT INTO public.orders (id, customer_id, sales_user_id, order_date, status, subtotal_amount, tax_amount, total_amount) VALUES
    ('OD00000001', 'CS00000001', 'E0001', '2026-10-05', 1, 12500.00, 1200.00, 13700.00),
    ('OD00000002', 'CS00000002', 'E0002', '2026-10-06', 1, 6000.00, 0.00, 6000.00);

INSERT INTO public.order_items (id, order_id, line_number, product_id, product_code, product_name, quantity, unit_price, tax_category, tax_rate, line_amount, tax_amount) VALUES
    ('OI00000001', 'OD00000001', 1, 'PR00000001', 'P-1001', 'ボールペン（黒）', 10, 1000.00, 1, 10.00, 10000.00, 1000.00),
    ('OI00000002', 'OD00000001', 2, 'PR00000002', 'F-2001', 'ミネラルウォーター 500ml', 5, 500.00, 2, 8.00, 2500.00, 200.00),
    ('OI00000003', 'OD00000002', 1, 'PR00000003', 'B-3001', '業務マニュアル（書籍）', 2, 3000.00, 3, 0.00, 6000.00, 0.00);

INSERT INTO public.order_status_histories (id, order_id, from_status, to_status, created_by) VALUES
    ('OH00000001', 'OD00000001', NULL, 1, 'E0001'),
    ('OH00000002', 'OD00000002', NULL, 1, 'E0002');

-- 採番シーケンスを投入済みの最大値まで進める
SELECT setval('public.seq_id_e', 2);
SELECT setval('public.seq_id_c', 2);
SELECT setval('public.seq_id_cs', 3);
SELECT setval('public.seq_id_cc', 3);
SELECT setval('public.seq_id_as', 3);
SELECT setval('public.seq_id_pr', 4);
SELECT setval('public.seq_id_od', 2);
SELECT setval('public.seq_id_oi', 3);
SELECT setval('public.seq_id_oh', 2);

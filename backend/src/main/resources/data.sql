UPDATE users
SET password_hash = CASE username
    WHEN 'operator' THEN '$2a$10$zi0cAxzLY9Vghhyd0/bQteLbDGOQ.iV3KukhoxZw5ByVm3/XVv6Ce'
    WHEN 'auditor' THEN '$2a$10$N5xfxXJtWSQkYiDQ51HyU.2wJE/hQR.Su98asGDEd7iR36WqQw.ye'
END
WHERE username IN ('operator', 'auditor')
  AND password_hash LIKE '<BCRYPT_HASH_%>';

INSERT INTO invoice_type_config (type, vat_rate, withholding_rate, active)
VALUES
    ('NATIONAL', 0.19, 0.00, true),
    ('EXPORT', 0.00, 0.00, true),
    ('GOVERNMENT', 0.19, 0.05, true)
ON CONFLICT (type) DO NOTHING;

INSERT INTO users (username, password_hash, role, enabled, created_at)
VALUES
    ('operator', '$2a$10$zi0cAxzLY9Vghhyd0/bQteLbDGOQ.iV3KukhoxZw5ByVm3/XVv6Ce', 'OPERATOR', true, CURRENT_TIMESTAMP),
    ('auditor', '$2a$10$N5xfxXJtWSQkYiDQ51HyU.2wJE/hQR.Su98asGDEd7iR36WqQw.ye', 'AUDITOR', true, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

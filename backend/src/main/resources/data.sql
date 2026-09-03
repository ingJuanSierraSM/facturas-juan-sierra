INSERT INTO invoice_type_config (type, vat_rate, withholding_rate, active)
VALUES
    ('NATIONAL', 0.19, 0.00, true),
    ('EXPORT', 0.00, 0.00, true),
    ('GOVERNMENT', 0.19, 0.05, true)
ON CONFLICT (type) DO NOTHING;

INSERT INTO users (username, password_hash, role, enabled, created_at)
VALUES
    ('operator', '<BCRYPT_HASH_OPERATOR>', 'OPERATOR', true, CURRENT_TIMESTAMP),
    ('auditor', '<BCRYPT_HASH_AUDITOR>', 'AUDITOR', true, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

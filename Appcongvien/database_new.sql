-- ============================================================
-- DATABASE: Park Adventure - Smart Card Management System
-- Engine: MySQL / MariaDB
-- Version: 2.0 (Server-side balance, Physical card only)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ============================================================
-- NHÓM 1: IDENTITY
-- ============================================================

DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    account_id   CHAR(36)     NOT NULL,
    phone_number VARCHAR(15)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role         ENUM('USER','STAFF','ADMIN') NOT NULL DEFAULT 'USER',
    status       ENUM('ACTIVE','BANNED')      NOT NULL DEFAULT 'ACTIVE',
    last_login   DATETIME     NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id),
    UNIQUE KEY uq_accounts_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Hồ sơ người dùng + ví tiền
-- Áp dụng cho cả người dùng có app (Case B) lẫn tài khoản tạm tại quầy (Case A)
CREATE TABLE users (
    user_id        CHAR(36)      NOT NULL,
    account_id     CHAR(36)      NOT NULL,
    full_name      VARCHAR(100)  NULL,
    email          VARCHAR(100)  NULL,
    date_of_birth  DATE          NULL,
    gender         ENUM('MALE','FEMALE','OTHER') NULL,
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    avatar_url     TEXT          NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_account (account_id),
    CONSTRAINT fk_users_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admins (
    admin_id      CHAR(36)    NOT NULL,
    account_id    CHAR(36)    NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    employee_code VARCHAR(20)  NOT NULL,
    role_level    ENUM('STAFF','ADMIN') NOT NULL DEFAULT 'STAFF',
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
    last_action_at DATETIME   NULL,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_id),
    UNIQUE KEY uq_admins_account (account_id),
    UNIQUE KEY uq_admins_employee_code (employee_code),
    CONSTRAINT fk_admins_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHÓM 2: SMART CARD (VẬT LÝ)
-- ============================================================

DROP TABLE IF EXISTS card_requests;
DROP TABLE IF EXISTS cards;

-- Chỉ quản lý thẻ vật lý NFC
-- deposit_status: NONE (chưa phát), PAID (đã thu cọc), REFUNDED (đã hoàn), FORFEITED (mất thẻ - không hoàn)
CREATE TABLE cards (
    card_id          CHAR(36)     NOT NULL,
    physical_card_uid VARCHAR(50) NOT NULL COMMENT 'UID vật lý của thẻ NFC',
    card_name        VARCHAR(50)  NULL,
    user_id          CHAR(36)     NULL COMMENT 'NULL = thẻ chưa liên kết với tài khoản nào',
    status           ENUM('AVAILABLE','ACTIVE','BLOCKED') NOT NULL DEFAULT 'AVAILABLE',
    deposit_amount   DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    deposit_status   ENUM('NONE','PAID','REFUNDED','FORFEITED') NOT NULL DEFAULT 'NONE',
    issued_at        DATETIME     NULL,
    blocked_at       DATETIME     NULL,
    blocked_reason   TEXT         NULL,
    last_used_at     DATETIME     NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (card_id),
    UNIQUE KEY uq_cards_uid (physical_card_uid),
    KEY idx_cards_user (user_id),
    KEY idx_cards_status (status),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Yêu cầu cấp thẻ từ app trước khi đến quầy (Case B)
CREATE TABLE card_requests (
    request_id          CHAR(36)      NOT NULL,
    user_id             CHAR(36)      NOT NULL,
    status              ENUM('PENDING','APPROVED','REJECTED','COMPLETED') NOT NULL DEFAULT 'PENDING',
    deposit_paid_online BOOLEAN       NOT NULL DEFAULT FALSE,
    deposit_amount      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    note                TEXT          NULL,
    approved_by         CHAR(36)      NULL COMMENT 'admin_id của nhân viên xử lý',
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (request_id),
    KEY idx_card_requests_user (user_id),
    KEY idx_card_requests_status (status),
    CONSTRAINT fk_card_requests_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_card_requests_admin FOREIGN KEY (approved_by) REFERENCES admins(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHÓM 3: GAME / VẬN HÀNH
-- ============================================================

DROP TABLE IF EXISTS game_reviews;
DROP TABLE IF EXISTS game_play_logs;
DROP TABLE IF EXISTS terminals;
DROP TABLE IF EXISTS games;

CREATE TABLE games (
    game_id           CHAR(36)      NOT NULL,
    name              VARCHAR(100)  NOT NULL,
    description       TEXT          NULL,
    short_description VARCHAR(255)  NULL,
    category          VARCHAR(50)   NULL,
    price_per_turn    DECIMAL(10,2) NOT NULL,
    duration_minutes  INT           NULL,
    location          VARCHAR(100)  NULL,
    thumbnail_url     TEXT          NULL,
    gallery_urls      TEXT          NULL COMMENT 'JSON array of URLs',
    age_required      INT           NULL,
    height_required   INT           NULL COMMENT 'Chiều cao tối thiểu (cm)',
    max_capacity      INT           NULL,
    status            ENUM('ACTIVE','INACTIVE','MAINTENANCE') NOT NULL DEFAULT 'ACTIVE',
    risk_level        INT           NULL COMMENT '1=thấp, 2=trung bình, 3=cao',
    is_featured       BOOLEAN       NOT NULL DEFAULT FALSE,
    average_rating    DECIMAL(2,1)  NOT NULL DEFAULT 0.0,
    total_reviews     INT           NOT NULL DEFAULT 0,
    total_plays       INT           NOT NULL DEFAULT 0,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (game_id),
    KEY idx_games_status (status),
    KEY idx_games_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thiết bị đọc thẻ tại các trò chơi
CREATE TABLE terminals (
    terminal_id    CHAR(36)    NOT NULL,
    name           VARCHAR(100) NOT NULL,
    game_id        CHAR(36)    NULL,
    terminal_type  ENUM('ENTRY_GATE','GAME_READER') NOT NULL DEFAULT 'GAME_READER',
    location       VARCHAR(100) NULL,
    status         ENUM('ONLINE','OFFLINE') NOT NULL DEFAULT 'ONLINE',
    ip_address     VARCHAR(45)  NULL,
    last_heartbeat DATETIME     NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (terminal_id),
    KEY idx_terminals_game (game_id),
    CONSTRAINT fk_terminals_game FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lịch sử mỗi lượt chơi: quẹt thẻ → server trừ tiền → ghi log
CREATE TABLE game_play_logs (
    log_id         CHAR(36)      NOT NULL,
    user_id        CHAR(36)      NOT NULL,
    game_id        CHAR(36)      NOT NULL,
    terminal_id    CHAR(36)      NULL,
    card_id        CHAR(36)      NULL,
    method         ENUM('CARD','BALANCE') NOT NULL DEFAULT 'CARD',
    amount_charged DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Số tiền đã trừ cho lượt chơi này',
    played_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    KEY idx_game_play_logs_user (user_id),
    KEY idx_game_play_logs_game (game_id),
    KEY idx_game_play_logs_card (card_id),
    KEY idx_game_play_logs_played_at (played_at),
    CONSTRAINT fk_game_play_logs_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_game_play_logs_game FOREIGN KEY (game_id) REFERENCES games(game_id),
    CONSTRAINT fk_game_play_logs_terminal FOREIGN KEY (terminal_id) REFERENCES terminals(terminal_id) ON DELETE SET NULL,
    CONSTRAINT fk_game_play_logs_card FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE game_reviews (
    review_id        CHAR(36) NOT NULL,
    user_id          CHAR(36) NOT NULL,
    game_id          CHAR(36) NOT NULL,
    rating           INT      NOT NULL COMMENT '1-5',
    comment          TEXT     NULL,
    is_verified_play BOOLEAN  NOT NULL DEFAULT FALSE COMMENT 'true nếu user đã thực sự chơi game này',
    is_visible       BOOLEAN  NOT NULL DEFAULT TRUE,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    KEY idx_game_reviews_game (game_id),
    KEY idx_game_reviews_user (user_id),
    CONSTRAINT fk_game_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_game_reviews_game FOREIGN KEY (game_id) REFERENCES games(game_id),
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHÓM 4: WALLET / PAYMENT
-- ============================================================

DROP TABLE IF EXISTS payment_records;
DROP TABLE IF EXISTS balance_transactions;

-- Mọi thay đổi số dư đều được ghi vào đây
-- type: TOPUP=nạp tiền, PAYMENT=trừ tiền chơi, REFUND=hoàn tiền,
--       DEPOSIT_PAID=thu tiền cọc, DEPOSIT_REFUND=hoàn cọc khi trả thẻ,
--       DEPOSIT_FORFEITED=mất cọc do mất thẻ, ADJUSTMENT=điều chỉnh bởi admin
CREATE TABLE balance_transactions (
    transaction_id CHAR(36)      NOT NULL,
    user_id        CHAR(36)      NOT NULL,
    amount         DECIMAL(15,2) NOT NULL COMMENT 'Dương = cộng, âm = trừ',
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after  DECIMAL(15,2) NOT NULL,
    type           ENUM('TOPUP','PAYMENT','REFUND','DEPOSIT_PAID','DEPOSIT_REFUND','DEPOSIT_FORFEITED','ADJUSTMENT') NOT NULL,
    reference_type VARCHAR(50)   NULL COMMENT 'Loại đối tượng tham chiếu (game_play_log, card, card_request...)',
    reference_id   CHAR(36)      NULL COMMENT 'ID của đối tượng tham chiếu',
    description    TEXT          NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     CHAR(36)      NULL COMMENT 'account_id của người tạo giao dịch (nhân viên hoặc hệ thống)',
    PRIMARY KEY (transaction_id),
    KEY idx_balance_transactions_user (user_id),
    KEY idx_balance_transactions_type (type),
    KEY idx_balance_transactions_created_at (created_at),
    CONSTRAINT fk_balance_transactions_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ghi nhận các giao dịch thanh toán qua cổng thanh toán (nạp tiền online)
CREATE TABLE payment_records (
    payment_id    CHAR(36)      NOT NULL,
    user_id       CHAR(36)      NOT NULL,
    method        VARCHAR(20)   NOT NULL COMMENT 'VNPAY, MOMO, ZALOPAY, CASH...',
    amount        DECIMAL(15,2) NOT NULL,
    status        ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
    external_ref_id VARCHAR(100) NULL COMMENT 'Mã tham chiếu từ cổng thanh toán',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    KEY idx_payment_records_user (user_id),
    KEY idx_payment_records_status (status),
    CONSTRAINT fk_payment_records_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHÓM 5: NOTIFICATION / SUPPORT / CONTENT
-- ============================================================

DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS support_messages;
DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
    notification_id CHAR(36)     NOT NULL,
    user_id         CHAR(36)     NOT NULL,
    type            ENUM('SYSTEM','CARD_SWIPE','TOPUP','REFUND','CARD_BLOCKED','GENERAL') NOT NULL DEFAULT 'GENERAL',
    title           VARCHAR(200) NOT NULL,
    message         TEXT         NOT NULL,
    data            TEXT         NULL COMMENT 'JSON payload cho deep link hoặc dữ liệu thêm',
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at         DATETIME     NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    KEY idx_notifications_user (user_id),
    KEY idx_notifications_is_read (is_read),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE support_messages (
    message_id  CHAR(36)  NOT NULL,
    user_id     CHAR(36)  NOT NULL,
    sender_id   CHAR(36)  NOT NULL,
    sender_type ENUM('USER','STAFF','ADMIN') NOT NULL,
    content     TEXT      NOT NULL,
    is_read     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id),
    KEY idx_support_messages_user (user_id),
    CONSTRAINT fk_support_messages_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE announcements (
    announcement_id CHAR(36)     NOT NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT         NOT NULL,
    image_url       TEXT         NULL,
    priority        ENUM('LOW','NORMAL','HIGH','URGENT') NOT NULL DEFAULT 'NORMAL',
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    start_date      DATETIME     NULL,
    end_date        DATETIME     NULL,
    created_by      CHAR(36)     NULL COMMENT 'account_id của admin tạo thông báo',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (announcement_id),
    KEY idx_announcements_active (is_active),
    KEY idx_announcements_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHÓM 6: RSA SMART CARD AUTHENTICATION
-- ============================================================

DROP TABLE IF EXISTS rsa_public_keys;

-- Lưu RSA public key của mỗi thẻ để server verify chữ ký
-- PK là card_id (1 thẻ = 1 RSA keypair)
-- Challenge để xác thực giữ trong memory của service, không lưu DB
CREATE TABLE rsa_public_keys (
    card_id        CHAR(36) NOT NULL,
    public_key_pem TEXT     NOT NULL COMMENT 'RSA public key dạng PEM',
    status         ENUM('ACTIVE','REVOKED') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (card_id),
    CONSTRAINT fk_rsa_public_keys_card FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- QUAN HỆ TÓM TẮT:
--   accounts 1-1 users
--   accounts 1-1 admins
--   users 1-n cards
--   cards 1-1 rsa_public_keys
--   users 1-n card_requests
--   users 1-n balance_transactions
--   users 1-n game_play_logs
--   cards 1-n game_play_logs
--   games 1-n game_play_logs
--   games 1-n terminals
--   games 1-n game_reviews
-- ============================================================

-- JWT 인증 시스템을 위한 테이블 생성

-- 기존 테이블 삭제 (주의: 모든 데이터가 삭제됩니다!)
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS corrections CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- users 테이블 생성 (password 포함)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),  -- BCrypt 암호화된 비밀번호
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- user_roles 테이블 (User 엔티티의 @ElementCollection)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- refresh_tokens 테이블
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens(expiry_date);

-- corrections 테이블 생성
CREATE TABLE corrections (
    id BIGSERIAL PRIMARY KEY,
    origin_sentence VARCHAR(1000) NOT NULL,
    corrected_sentence VARCHAR(1000) NOT NULL,
    feedback TEXT NOT NULL,
    feedback_type VARCHAR(50) NOT NULL DEFAULT 'GRAMMAR',
    score INT,
    is_favorite BOOLEAN DEFAULT FALSE,
    memo TEXT,
    origin_translation TEXT,
    corrected_translation TEXT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_correction_user_id ON corrections(user_id);
CREATE INDEX IF NOT EXISTS idx_correction_created_at ON corrections(created_at);
CREATE INDEX IF NOT EXISTS idx_correction_feedback_type ON corrections(feedback_type);
CREATE INDEX IF NOT EXISTS idx_correction_is_favorite ON corrections(is_favorite);
CREATE INDEX IF NOT EXISTS idx_correction_score ON corrections(score);
CREATE INDEX IF NOT EXISTS idx_correction_user_created ON corrections(user_id, created_at);

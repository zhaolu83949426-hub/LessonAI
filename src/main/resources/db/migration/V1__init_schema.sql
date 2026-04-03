CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    account VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户主键';
COMMENT ON COLUMN users.account IS '登录账号';
COMMENT ON COLUMN users.password_hash IS '密码哈希';
COMMENT ON COLUMN users.nickname IS '用户昵称';
COMMENT ON COLUMN users.deleted IS '逻辑删除标记';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_account_not_deleted ON users (account) WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS prompt_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE prompt_templates IS '提示词模板表';
COMMENT ON COLUMN prompt_templates.id IS '模板主键';
COMMENT ON COLUMN prompt_templates.user_id IS '所属用户ID';
COMMENT ON COLUMN prompt_templates.name IS '模板名称';
COMMENT ON COLUMN prompt_templates.content IS '模板内容';
COMMENT ON COLUMN prompt_templates.deleted IS '逻辑删除标记';
COMMENT ON COLUMN prompt_templates.created_at IS '创建时间';
COMMENT ON COLUMN prompt_templates.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_prompt_templates_user_id ON prompt_templates (user_id);

CREATE TABLE IF NOT EXISTS llm_configs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    provider_type VARCHAR(32) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    model_code VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE llm_configs IS '大模型配置表';
COMMENT ON COLUMN llm_configs.id IS '模型配置主键';
COMMENT ON COLUMN llm_configs.name IS '配置名称';
COMMENT ON COLUMN llm_configs.provider_type IS '提供商类型';
COMMENT ON COLUMN llm_configs.base_url IS '接口基础地址';
COMMENT ON COLUMN llm_configs.api_key IS '接口鉴权Key';
COMMENT ON COLUMN llm_configs.model_code IS '模型编码';
COMMENT ON COLUMN llm_configs.enabled IS '是否启用';
COMMENT ON COLUMN llm_configs.is_default IS '是否默认模型';
COMMENT ON COLUMN llm_configs.sort_order IS '排序值';
COMMENT ON COLUMN llm_configs.deleted IS '逻辑删除标记';
COMMENT ON COLUMN llm_configs.created_at IS '创建时间';
COMMENT ON COLUMN llm_configs.updated_at IS '更新时间';

CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    template_id BIGINT NOT NULL,
    llm_config_id BIGINT NOT NULL,
    current_result_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_sessions IS '会话表';
COMMENT ON COLUMN chat_sessions.id IS '会话主键';
COMMENT ON COLUMN chat_sessions.user_id IS '所属用户ID';
COMMENT ON COLUMN chat_sessions.title IS '会话标题';
COMMENT ON COLUMN chat_sessions.template_id IS '关联模板ID';
COMMENT ON COLUMN chat_sessions.llm_config_id IS '关联模型配置ID';
COMMENT ON COLUMN chat_sessions.current_result_id IS '当前教案结果ID';
COMMENT ON COLUMN chat_sessions.deleted IS '逻辑删除标记';
COMMENT ON COLUMN chat_sessions.created_at IS '创建时间';
COMMENT ON COLUMN chat_sessions.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON chat_sessions (user_id);

CREATE TABLE IF NOT EXISTS lesson_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    llm_config_id BIGINT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    input_payload JSONB NOT NULL,
    final_prompt TEXT NOT NULL,
    result_content TEXT NOT NULL,
    edited_content TEXT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE lesson_records IS '教案记录表';
COMMENT ON COLUMN lesson_records.id IS '教案记录主键';
COMMENT ON COLUMN lesson_records.user_id IS '所属用户ID';
COMMENT ON COLUMN lesson_records.session_id IS '所属会话ID';
COMMENT ON COLUMN lesson_records.template_id IS '使用模板ID';
COMMENT ON COLUMN lesson_records.llm_config_id IS '使用模型配置ID';
COMMENT ON COLUMN lesson_records.model_name IS '模型名称快照';
COMMENT ON COLUMN lesson_records.topic IS '教案主题';
COMMENT ON COLUMN lesson_records.input_payload IS '输入参数快照';
COMMENT ON COLUMN lesson_records.final_prompt IS '最终发送给模型的Prompt';
COMMENT ON COLUMN lesson_records.result_content IS '模型原始生成结果';
COMMENT ON COLUMN lesson_records.edited_content IS '用户编辑后的教案内容';
COMMENT ON COLUMN lesson_records.deleted IS '逻辑删除标记';
COMMENT ON COLUMN lesson_records.created_at IS '创建时间';
COMMENT ON COLUMN lesson_records.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_lesson_records_session_id ON lesson_records (session_id);

CREATE TABLE IF NOT EXISTS session_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    lesson_record_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE session_messages IS '会话消息表';
COMMENT ON COLUMN session_messages.id IS '消息主键';
COMMENT ON COLUMN session_messages.session_id IS '所属会话ID';
COMMENT ON COLUMN session_messages.role IS '消息角色';
COMMENT ON COLUMN session_messages.content IS '消息内容';
COMMENT ON COLUMN session_messages.lesson_record_id IS '关联教案记录ID';
COMMENT ON COLUMN session_messages.deleted IS '逻辑删除标记';
COMMENT ON COLUMN session_messages.created_at IS '创建时间';
COMMENT ON COLUMN session_messages.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_session_messages_session_id ON session_messages (session_id);

INSERT INTO llm_configs (name, provider_type, base_url, api_key, model_code, enabled, is_default, sort_order)
SELECT '默认模型', 'OPENAI_COMPATIBLE', 'http://192.168.12.22:7898/v1', 'sk-dd5fcc6f18bcc70fbc56c5f6565de507bf37198e61e5f1a7302d81adeda80c9e', 'gpt-4.1', TRUE, TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM llm_configs WHERE deleted = FALSE);

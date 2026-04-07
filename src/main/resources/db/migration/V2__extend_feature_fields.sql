ALTER TABLE prompt_templates
    ADD COLUMN IF NOT EXISTS category VARCHAR(32) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS tags VARCHAR(255) NOT NULL DEFAULT '';

COMMENT ON COLUMN prompt_templates.category IS '模板分类';
COMMENT ON COLUMN prompt_templates.tags IS '模板标签，逗号分隔';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS default_template_id BIGINT,
    ADD COLUMN IF NOT EXISTS default_style VARCHAR(50) NOT NULL DEFAULT '';

COMMENT ON COLUMN users.default_template_id IS '默认模板ID';
COMMENT ON COLUMN users.default_style IS '默认教案风格偏好';

ALTER TABLE lesson_records
    ADD COLUMN IF NOT EXISTS feedback_rating INT,
    ADD COLUMN IF NOT EXISTS feedback_level VARCHAR(32),
    ADD COLUMN IF NOT EXISTS feedback_comment VARCHAR(255) NOT NULL DEFAULT '';

COMMENT ON COLUMN lesson_records.feedback_rating IS '质量评分';
COMMENT ON COLUMN lesson_records.feedback_level IS '质量等级';
COMMENT ON COLUMN lesson_records.feedback_comment IS '质量反馈说明';

CREATE TABLE IF NOT EXISTS permission (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS role (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permission (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    number_of_failed_login_attempts INT NOT NULL DEFAULT 0,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    is_credentials_expired BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS app_user_role (
    application_user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (application_user_id, role_id),
    CONSTRAINT fk_app_user_role_user
        FOREIGN KEY (application_user_id) REFERENCES app_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_user_role_role
        FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);

ALTER TABLE comment
    ADD COLUMN user_id UUID,
    ADD CONSTRAINT fk_comment_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE;

ALTER TABLE comment
    ALTER COLUMN user_id SET NOT NULL;
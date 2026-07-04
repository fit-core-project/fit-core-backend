CREATE TABLE IF NOT EXISTS routine_programs (
    program_id        CHAR(36)     NOT NULL,
    user_id           VARCHAR(36)  NOT NULL,
    name              VARCHAR(255) NOT NULL,
    status            VARCHAR(16)  NOT NULL,
    current_position  INT          NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at      DATETIME,
    PRIMARY KEY (program_id),
    CONSTRAINT chk_routine_programs_position CHECK (current_position >= 1),
    CONSTRAINT chk_routine_programs_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED'))
);

CREATE INDEX idx_routine_programs_user_status ON routine_programs (user_id, status);

CREATE TABLE IF NOT EXISTS routine_program_items (
    program_item_id   CHAR(36)     NOT NULL,
    program_id        CHAR(36)     NOT NULL,
    position          INT          NOT NULL,
    routine_final_id  CHAR(36)     NOT NULL,
    title_snapshot    VARCHAR(255),
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (program_item_id),
    CONSTRAINT fk_routine_program_items_program
        FOREIGN KEY (program_id) REFERENCES routine_programs (program_id),
    CONSTRAINT fk_routine_program_items_final
        FOREIGN KEY (routine_final_id) REFERENCES routine_finals (routine_final_id),
    CONSTRAINT uq_routine_program_items_position UNIQUE (program_id, position),
    CONSTRAINT uq_routine_program_items_final UNIQUE (program_id, routine_final_id),
    CONSTRAINT chk_routine_program_items_position CHECK (position >= 1)
);

CREATE INDEX idx_routine_program_items_program ON routine_program_items (program_id, position);

CREATE TABLE IF NOT EXISTS routine_program_completion_events (
    event_id            CHAR(36) NOT NULL,
    program_id          CHAR(36) NOT NULL,
    program_item_id     CHAR(36) NOT NULL,
    workout_session_id  CHAR(36) NOT NULL,
    completed_position  INT      NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    CONSTRAINT fk_routine_program_events_program
        FOREIGN KEY (program_id) REFERENCES routine_programs (program_id),
    CONSTRAINT fk_routine_program_events_item
        FOREIGN KEY (program_item_id) REFERENCES routine_program_items (program_item_id),
    CONSTRAINT fk_routine_program_events_session
        FOREIGN KEY (workout_session_id) REFERENCES workout_sessions (workout_session_id),
    CONSTRAINT uq_routine_program_events_session UNIQUE (workout_session_id),
    CONSTRAINT uq_routine_program_events_program_item_session UNIQUE (program_id, program_item_id, workout_session_id),
    CONSTRAINT chk_routine_program_events_position CHECK (completed_position >= 1)
);

CREATE INDEX idx_routine_program_events_program ON routine_program_completion_events (program_id, created_at);

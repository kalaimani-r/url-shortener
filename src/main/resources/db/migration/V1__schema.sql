CREATE TABLE short_links (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL,
    long_url VARCHAR(2048) NOT NULL,
    long_url_hash VARCHAR(64) NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_short_links_code UNIQUE (short_code)
);

CREATE INDEX idx_short_links_code ON short_links (short_code);
CREATE INDEX idx_short_links_long_url_hash ON short_links (long_url_hash);

CREATE TABLE click_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL,
    user_agent VARCHAR(512),
    referrer VARCHAR(2048),
    client_ip VARCHAR(45),
    clicked_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_click_events_short_code ON click_events (short_code);

-- Table for UserAction
CREATE TABLE IF NOT EXISTS user_action (
                             id BIGSERIAL PRIMARY KEY,         -- Auto-generated ID
                             user_id BIGINT NOT NULL,          -- User identifier
                             event_id BIGINT NOT NULL,         -- Event identifier
                             action_type DOUBLE PRECISION NOT NULL, -- Action type as a string
                             timestamp BIGINT NOT NULL         -- Timestamp of the action
);

-- Table for EventSimilarity
CREATE TABLE IF NOT EXISTS event_similarity (
                                  id BIGSERIAL PRIMARY KEY,         -- Auto-generated ID
                                  event_id_a BIGINT NOT NULL,       -- First event ID
                                  event_id_b BIGINT NOT NULL,       -- Second event ID
                                  similarity DOUBLE PRECISION NOT NULL, -- Similarity value
                                  timestamp BIGINT NOT NULL         -- Timestamp of the calculation
);

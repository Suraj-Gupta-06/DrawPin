-- V6__create_media.sql
CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(50) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL,
    public_id VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    file_size BIGINT NOT NULL,
    width INT,
    height INT,
    aspect_ratio DOUBLE PRECISION,
    orientation VARCHAR(50),
    duration DOUBLE PRECISION,
    secure_url TEXT NOT NULL,
    thumbnail_url TEXT,
    folder VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Index for quick lookup by owner
CREATE INDEX idx_media_owner ON media(owner_user_id);
-- Index for quick lookup by checksum to prevent duplicates
CREATE INDEX idx_media_checksum ON media(checksum, owner_user_id);
-- Index for status filtering
CREATE INDEX idx_media_status ON media(status);
-- Index for public_id retrieval
CREATE INDEX idx_media_public_id ON media(public_id);

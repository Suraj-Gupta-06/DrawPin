-- V4__create_creator_profile.sql
CREATE TABLE creators (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(80) NOT NULL,
    bio VARCHAR(500),
    specialization VARCHAR(100),
    experience_years INT DEFAULT 0,
    skills JSONB,
    portfolio_website VARCHAR(255),
    social_links JSONB,
    is_available BOOLEAN NOT NULL DEFAULT true,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    
    followers_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    artworks_count INT NOT NULL DEFAULT 0,
    reviews_count INT NOT NULL DEFAULT 0,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for quick lookup by user_id
CREATE INDEX idx_creators_user_id ON creators(user_id);

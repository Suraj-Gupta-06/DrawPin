-- V5__create_categories.sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(255),
    image_url VARCHAR(255),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for quick lookup by parent_id (essential for tree queries)
CREATE INDEX idx_categories_parent_id ON categories(parent_category_id);
-- Index for quick lookup by slug
CREATE INDEX idx_categories_slug ON categories(slug);
-- Index for ordering when fetching active categories
CREATE INDEX idx_categories_active_order ON categories(is_active, display_order);

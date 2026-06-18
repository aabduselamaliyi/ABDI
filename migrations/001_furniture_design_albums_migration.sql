-- ============================================================================
-- BEKANSI AI SALES ASSISTANT PLATFORM
-- SQL MIGRATION: FURNITURE DESIGN ALBUM MODULE (v1.1.0)
-- Optimized for Tenant Isolation, High Concurrency, Indexing, and RLS
-- ============================================================================

-- Step 1: Create Categories Table for Design Albums
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(255) NOT NULL,
    name_am VARCHAR(255), -- Amharic translation
    name_om VARCHAR(255), -- Afaan Oromo translation
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_category_name UNIQUE (organization_id, name)
);

-- Indexing for fast tenant-specific lookups on categories
CREATE INDEX IF NOT EXISTS idx_categories_organization_id ON categories(organization_id);

-- Step 2: Create Design Albums Table (Core catalog)
CREATE TABLE IF NOT EXISTS design_albums (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    album_code VARCHAR(100) NOT NULL, -- e.g. 'BS-001'
    name VARCHAR(255) NOT NULL,
    design_style VARCHAR(100) DEFAULT 'Modern' NOT NULL, -- e.g. Modern, Scandinavian, Classic
    description_en TEXT NOT NULL,
    description_am TEXT,
    description_om TEXT,
    dimensions VARCHAR(255),
    material_options TEXT, -- comma-separated or list of timber options (Wanza, Mahogany, Grar)
    color_options TEXT, -- color stain listings
    estimated_production_time VARCHAR(100) DEFAULT '14 Days',
    price_range_lower DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    price_range_upper DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    popularity_score INT DEFAULT 80 NOT NULL, -- rating rank (0 to 100)
    tags TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_album_code UNIQUE (organization_id, album_code)
);

-- Indexing for design search parameters & filtering
CREATE INDEX IF NOT EXISTS idx_design_albums_organization_id ON design_albums(organization_id);
CREATE INDEX IF NOT EXISTS idx_design_albums_category_id ON design_albums(category_id);
CREATE INDEX IF NOT EXISTS idx_design_albums_code ON design_albums(album_code);

-- Step 3: Create Album Images Table (1-to-N normalized image set)
CREATE TABLE IF NOT EXISTS album_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE NOT NULL,
    display_order INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for lightning fast multi-image loading
CREATE INDEX IF NOT EXISTS idx_album_images_organization_id ON album_images(organization_id);
CREATE INDEX IF NOT EXISTS idx_album_images_album_id ON album_images(album_id);

-- Step 4: Create Customer Favorites Table
CREATE TABLE IF NOT EXISTS customer_favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_customer_favorite UNIQUE (organization_id, customer_id, album_id)
);

-- Indexing for favorites checks
CREATE INDEX IF NOT EXISTS idx_customer_favorites_organization_id ON customer_favorites(organization_id);
CREATE INDEX IF NOT EXISTS idx_customer_favorites_customer_id ON customer_favorites(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_favorites_album_id ON customer_favorites(album_id);

-- Step 5: Create Design Selections Table (Quotation/leads binder)
CREATE TABLE IF NOT EXISTS design_selections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL, -- Nullable to allow self-serve selections before agent contact
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    requirements TEXT, -- Custom adaptation specifics (e.g. 'extra soft-close drawers')
    budget DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    status VARCHAR(50) DEFAULT 'pending' NOT NULL, -- pending, quoted, ordered, canceled
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexing for selections in lead pipelines
CREATE INDEX IF NOT EXISTS idx_design_selections_organization_id ON design_selections(organization_id);
CREATE INDEX IF NOT EXISTS idx_design_selections_customer_id ON design_selections(customer_id);
CREATE INDEX IF NOT EXISTS idx_design_selections_lead_id ON design_selections(lead_id);
CREATE INDEX IF NOT EXISTS idx_design_selections_album_id ON design_selections(album_id);

-- ============================================================================
-- ROW-LEVEL SECURITY (RLS) FOR MULTI-TENANT ISOLATION
-- ============================================================================

ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE design_albums ENABLE ROW LEVEL SECURITY;
ALTER TABLE album_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE design_selections ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_categories ON categories
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_design_albums ON design_albums
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_album_images ON album_images
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_customer_favorites ON customer_favorites
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_design_selections ON design_selections
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

-- ============================================================================
-- AUDIT AND TEMPORAL TRIGGERS
-- ============================================================================

CREATE TRIGGER update_categories_modtime 
    BEFORE UPDATE ON categories 
    FOR EACH ROW 
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_design_albums_modtime 
    BEFORE UPDATE ON design_albums 
    FOR EACH ROW 
    EXECUTE FUNCTION update_modified_column();

CREATE TRIGGER update_design_selections_modtime 
    BEFORE UPDATE ON design_selections 
    FOR EACH ROW 
    EXECUTE FUNCTION update_modified_column();

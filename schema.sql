-- ============================================================================
-- BEKANSI AI SALES ASSISTANT PLATFORM
-- PRODUCTION-READY MULTI-TENANT POSTGRESQL SCHEMA (v1.0.0)
-- Optimized for Tenant Isolation, High Concurrency, Multilingual JSONB fields,
-- Row-Level Security (RLS), and Enterprise CRM/ERP synchronizations.
-- ============================================================================

-- Enable clean UUID generator and cryptographic functions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- GLOBAL ENUMS AND DATA TYPES
-- ============================================================================

CREATE TYPE user_role AS ENUM (
    'super_admin',     -- Platform-wide superadmin
    'tenant_admin',    -- Owner of an organization/tenant
    'manager',         -- Sales / Factory manager managing leads/quotes
    'sales_agent',     -- Ground floor sales agent closing deals
    'ai_assistant'     -- Omnichannel AI Sales Agent identity
);

CREATE TYPE lead_status AS ENUM (
    'new',
    'contacted',
    'qualified',
    'proposal_sent',
    'negotiation',
    'won',
    'lost'
);

CREATE TYPE communication_channel AS ENUM (
    'whatsapp',
    'telegram',
    'facebook_messenger',
    'web_chat',
    'mobile_app',
    'voice'
);

CREATE TYPE conversation_status AS ENUM (
    'bot_active',      -- AI Assistant handles fully
    'human_required',  -- Flagged for human agent handover
    'human_active',    -- Human agent currently handling
    'archived'         -- Interaction resolved
);

-- ============================================================================
-- TENANCY: ORGANIZATIONS (TENANTS)
-- ============================================================================

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) UNIQUE NOT NULL,
    logo_url TEXT,
    billing_plan VARCHAR(50) DEFAULT 'growth', -- startup, growth, enterprise
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Index on subdomain for fast domain-router resolution in multi-tenant SaaS
CREATE INDEX idx_organizations_subdomain ON organizations(subdomain);

-- ============================================================================
-- USER MANAGEMENT (RBAC INCLUDED)
-- ============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role DEFAULT 'sales_agent'::user_role NOT NULL,
    phone_number VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_email UNIQUE (organization_id, email)
);

CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_email ON users(email);

-- ============================================================================
-- CUSTOMERS (OMNICHANNEL IDENTITIES)
-- ============================================================================

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone_number VARCHAR(100) NOT NULL, -- Core identifier across channels (WhatsApp, Telegram)
    preferred_language VARCHAR(10) DEFAULT 'am', -- am: Amharic, om: Afaan Oromo, en: English
    telegram_id VARCHAR(100),
    facebook_psid VARCHAR(100), -- Page-Scoped ID for FB Messenger
    segment_tags VARCHAR(100)[] DEFAULT '{}', -- Enterprise segmentation
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_phone UNIQUE (organization_id, phone_number)
);

CREATE INDEX idx_customers_organization_id ON customers(organization_id);
CREATE INDEX idx_customers_phone ON customers(phone_number);
CREATE INDEX idx_customers_social_ids ON customers(telegram_id, facebook_psid);

-- ============================================================================
-- LEADS & PIPELINE TRACKING
-- ============================================================================

CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    assigned_agent_id UUID REFERENCES users(id) ON DELETE SET NULL,
    source VARCHAR(100) DEFAULT 'whatsapp',
    status lead_status DEFAULT 'new'::lead_status NOT NULL,
    lead_score INT DEFAULT 50, -- Computed qualification level (0 to 100)
    requirements TEXT, -- Raw transcribed or captured requirement text
    estimated_budget DECIMAL(12, 2),
    notes TEXT,
    follow_up_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_leads_organization_id ON leads(organization_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned_agent ON leads(assigned_agent_id);

-- ============================================================================
-- MULTILINGUAL PRODUCT CATALOG
-- ============================================================================

CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    -- Localized metadata: {"en": "Kitchen", "am": "የኩሽና እቃዎች", "om": "Kila Kitchen"}
    names JSONB NOT NULL,
    slug VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_category_slug UNIQUE (organization_id, slug)
);

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    category_id UUID REFERENCES product_categories(id) ON DELETE SET NULL,
    sku VARCHAR(100) NOT NULL,
    -- Localized texts to prevent table bloat:
    -- names structure: {"en": "Wanza Gara Sofa", "am": "ዋንዛ ሶፋ ጋራ", "om": "Sofa Gara Wanza"}
    names JSONB NOT NULL,
    descriptions JSONB NOT NULL,
    -- Structured technical configs: dimensions, material type (hardwood, mahogany)
    specifications JSONB DEFAULT '{}'::jsonb NOT NULL,
    price DECIMAL(12, 2) NOT NULL, -- Absolute ERP base price in ETB (birr)
    currency VARCHAR(10) DEFAULT 'ETB' NOT NULL,
    inventory_count INT DEFAULT 0 NOT NULL,
    image_urls TEXT[] DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_sku UNIQUE (organization_id, sku)
);

CREATE INDEX idx_products_organization_id ON products(organization_id);
CREATE INDEX idx_products_sku ON products(sku);
-- JSONB Indexing to facilitate fast searching of Amharic/Oromo terms
CREATE INDEX idx_products_names_jsonb ON products USING gin (names);

-- ============================================================================
-- OMNICHANNEL CONVERSATION ROUTER
-- ============================================================================

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    channel communication_channel NOT NULL,
    external_chat_id VARCHAR(255) NOT NULL, -- Actual WhatsApp/Telegram Chat/Group ID
    status conversation_status DEFAULT 'bot_active'::conversation_status NOT NULL,
    last_message_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_org_channel_chat UNIQUE (organization_id, channel, external_chat_id)
);

CREATE INDEX idx_conversations_organization_id ON conversations(organization_id);
CREATE INDEX idx_conversations_external_chat ON conversations(channel, external_chat_id);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE NOT NULL,
    sender_type VARCHAR(50) NOT NULL, -- 'customer', 'human_agent', 'ai_assistant'
    sender_id UUID, -- References users(id) if non-customer (Nullable for security)
    raw_content TEXT NOT NULL,
    -- Multilingual translations cached instantly by the middleware: {"en": "...", "om": "..."}
    translated_content JSONB DEFAULT '{}'::jsonb NOT NULL,
    -- Store message structural attachments, media URLs, delivery statuses, API responses
    metadata JSONB DEFAULT '{}'::jsonb NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- ============================================================================
-- PIPELINE: QUOTATIONS AND ORDERS (ILILI ERP READINESS)
-- ============================================================================

CREATE TABLE quotations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    quotation_number VARCHAR(100) NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    tax DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'draft' NOT NULL, -- draft, sent, approved, declined, expired, converted_to_order
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_quotation_number UNIQUE (organization_id, quotation_number)
);

CREATE INDEX idx_quotations_organization_id ON quotations(organization_id);
CREATE INDEX idx_quotations_customer_id ON quotations(customer_id);

CREATE TABLE quotation_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    quotation_id UUID REFERENCES quotations(id) ON DELETE CASCADE NOT NULL,
    product_id UUID REFERENCES products(id) ON DELETE RESTRICT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL
);

CREATE INDEX idx_quotation_items_quotation_id ON quotation_items(quotation_id);

-- ============================================================================
-- FURNITURE DESIGN ALBUM MODULE
-- ============================================================================

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(255) NOT NULL,
    name_am VARCHAR(255),
    name_om VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_category_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_categories_organization_id ON categories(organization_id);

CREATE TABLE design_albums (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    album_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    design_style VARCHAR(100) DEFAULT 'Modern' NOT NULL,
    description_en TEXT NOT NULL,
    description_am TEXT,
    description_om TEXT,
    dimensions VARCHAR(255),
    material_options TEXT,
    color_options TEXT,
    estimated_production_time VARCHAR(100) DEFAULT '14 Days',
    price_range_lower DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    price_range_upper DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    popularity_score INT DEFAULT 80 NOT NULL,
    tags TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_album_code UNIQUE (organization_id, album_code)
);

CREATE INDEX idx_design_albums_organization_id ON design_albums(organization_id);
CREATE INDEX idx_design_albums_category_id ON design_albums(category_id);
CREATE INDEX idx_design_albums_code ON design_albums(album_code);

CREATE TABLE album_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE NOT NULL,
    display_order INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_album_images_organization_id ON album_images(organization_id);
CREATE INDEX idx_album_images_album_id ON album_images(album_id);

CREATE TABLE customer_favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_organization_customer_favorite UNIQUE (organization_id, customer_id, album_id)
);

CREATE INDEX idx_customer_favorites_organization_id ON customer_favorites(organization_id);
CREATE INDEX idx_customer_favorites_customer_id ON customer_favorites(customer_id);
CREATE INDEX idx_customer_favorites_album_id ON customer_favorites(album_id);

CREATE TABLE design_selections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    lead_id UUID REFERENCES leads(id) ON DELETE SET NULL,
    album_id UUID REFERENCES design_albums(id) ON DELETE CASCADE NOT NULL,
    requirements TEXT,
    budget DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    status VARCHAR(50) DEFAULT 'pending' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_design_selections_organization_id ON design_selections(organization_id);
CREATE INDEX idx_design_selections_customer_id ON design_selections(customer_id);
CREATE INDEX idx_design_selections_lead_id ON design_selections(lead_id);
CREATE INDEX idx_design_selections_album_id ON design_selections(album_id);

-- ============================================================================
-- AUDITING AND AI TRAINING TELEMETRY
-- ============================================================================

CREATE TABLE ai_interaction_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE NOT NULL,
    message_id UUID REFERENCES messages(id) ON DELETE SET NULL,
    model_version VARCHAR(100) NOT NULL, -- e.g., 'gemini-1.5-pro' or 'gemini-2.0-flash'
    prompt_tokens INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    latency_ms INT DEFAULT 0,
    confidence_score DECIMAL(5, 4), -- 0.0000 to 1.0000
    intent_detected VARCHAR(100),
    system_instruction_hash VARCHAR(100),
    feedback_rating INT, -- Optional human satisfaction feedback (1 to 5)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_ai_logs_organization_id ON ai_interaction_logs(organization_id);

-- ============================================================================
-- ROW-LEVEL SECURITY (RLS) FOR MULTI-TENANT ISOLATION
-- ============================================================================

-- Step 1: Enable RLS on every tenant-specific table
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE quotations ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_interaction_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE design_albums ENABLE ROW LEVEL SECURITY;
ALTER TABLE album_images ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE design_selections ENABLE ROW LEVEL SECURITY;

-- Step 2: Establish the Tenant-Isolation Policies using a session-context variable
-- We assume application servers establish the session tenant by calling:
-- `SET LOCAL app.current_tenant_id = 'your-tenant-uuid'` inside a transaction.

CREATE POLICY tenant_isolation_users ON users
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_customers ON customers
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_leads ON leads
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_categories ON product_categories
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_products ON products
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_conversations ON conversations
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_quotations ON quotations
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

CREATE POLICY tenant_isolation_ai_logs ON ai_interaction_logs
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

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
-- SOCIAL MEDIA MARKETING & AUTOMATION QUEUES
-- ============================================================================

CREATE TABLE smm_posts (
    id SERIAL PRIMARY KEY,
    organization_id UUID DEFAULT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    image_url TEXT,
    platform VARCHAR(50) NOT NULL, 
    status VARCHAR(50) DEFAULT 'pending', 
    scheduled_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Enable RLS and isolate smm_posts
ALTER TABLE smm_posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_smm_posts ON smm_posts
    FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);

-- ============================================================================
-- AUDIT AND TEMPORAL TRIGGERS
-- ============================================================================

-- Automated trigger function to update the 'updated_at' columns on row change
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE TRIGGER update_organizations_modtime BEFORE UPDATE ON organizations FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_customers_modtime BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_leads_modtime BEFORE UPDATE ON leads FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_products_modtime BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_quotations_modtime BEFORE UPDATE ON quotations FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_categories_modtime BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_design_albums_modtime BEFORE UPDATE ON design_albums FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_design_selections_modtime BEFORE UPDATE ON design_selections FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_smm_posts_modtime BEFORE UPDATE ON smm_posts FOR EACH ROW EXECUTE FUNCTION update_modified_column();

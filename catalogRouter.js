/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * ENTERPRISE MULTI-TENANT FURNITURE CATALOG ROUTER AND CONTROLLER
 * ============================================================================
 * 
 * Implements a secure, high-performance Express.js router with controller 
 * actions to perform CRUD operations on the multitenant product catalog.
 *
 * Guarantees that:
 * 1. Every single database query executes strictly within the authenticated 
 *    tenant context, isolating organization_id automatically via RLS.
 * 2. Properly manages multilingual JSONB payloads (supporting English, Amharic,
 *    and Afaan Oromo).
 * 3. Handles robust validation, pagination, catalog filtering, and error handling.
 */

const express = require('express');
const logger = require('./logger');
const { tenantAuthenticator, injectScopedDbClient, requestResponseLogger } = require('./tenantMiddleware');

const router = express.Router();

// Apply request/response tracing middleware
router.use(requestResponseLogger);

/**
 * ============================================================================
 * CATALOG CONTROLLER IMPLEMENTATION
 * ============================================================================
 */
const CatalogController = {

    /**
     * Get Paginated and Filtered Products
     * 
     * Route: GET /api/v1/products
     * Query Parameters:
     *  - category_id: UUID
     *  - search: String (filters by SKU or multilingual names)
     *  - min_price: Decimal
     *  - max_price: Decimal
     *  - active_only: Boolean (default true)
     *  - locale: 'en' | 'am' | 'om' (sets translation fallback, defaults to 'am')
     *  - page: Integer (default 1)
     *  - limit: Integer (default 20)
     */
    getProducts: async (req, res) => {
        try {
            const {
                category_id,
                search,
                min_price,
                max_price,
                active_only = 'true',
                locale = 'am',
                page = 1,
                limit = 20
            } = req.query;

            const offset = (parseInt(page, 10) - 1) * parseInt(limit, 10);
            const queryLimit = parseInt(limit, 10);

            // Execute query inside our tenant-isolated transaction
            const result = await req.dbExecute(async (txClient) => {
                let sqlQuery = `
                    SELECT 
                        p.id,
                        p.sku,
                        p.names->>$1 AS name,      -- Extract localized name dynamically
                        p.descriptions->>$1 AS description, -- Extract localized description dynamically
                        p.names AS names_raw,       -- Keep full JSONB bundle for editing
                        p.descriptions AS descriptions_raw,
                        p.specifications,
                        p.price,
                        p.currency,
                        p.inventory_count,
                        p.image_urls,
                        p.is_active,
                        p.category_id,
                        pc.names->>$1 AS category_name,
                        p.created_at
                    FROM products p
                    LEFT JOIN product_categories pc ON p.category_id = pc.id
                    WHERE 1=1
                `;
                const queryParams = [locale];
                let paramIndex = 2;

                // Category Filter
                if (category_id) {
                    sqlQuery += ` AND p.category_id = $${paramIndex}`;
                    queryParams.push(category_id);
                    paramIndex++;
                }

                // Search Filter: Matches SKU or localized JSONB fields (names, specifications)
                if (search) {
                    sqlQuery += ` AND (p.sku ILIKE $${paramIndex} OR p.names->>'en' ILIKE $${paramIndex} OR p.names->>'am' ILIKE $${paramIndex} OR p.names->>'om' ILIKE $${paramIndex})`;
                    queryParams.push(`%${search}%`);
                    paramIndex++;
                }

                // Price Controls
                if (min_price) {
                    sqlQuery += ` AND p.price >= $${paramIndex}`;
                    queryParams.push(parseFloat(min_price));
                    paramIndex++;
                }
                if (max_price) {
                    sqlQuery += ` AND p.price <= $${paramIndex}`;
                    queryParams.push(parseFloat(max_price));
                    paramIndex++;
                }

                // Active State Filter
                if (active_only === 'true') {
                    sqlQuery += ` AND p.is_active = TRUE`;
                }

                // Count total for pagination meta
                const countQueryStr = `SELECT COUNT(*) FROM (${sqlQuery}) AS count_query`;
                const { rows: countRows } = await txClient.query(countQueryStr, queryParams);
                const totalCount = parseInt(countRows[0].count, 10);

                // Add sorting, pagination limits dynamically
                sqlQuery += ` ORDER BY p.created_at DESC LIMIT $${paramIndex} OFFSET $${paramIndex + 1}`;
                queryParams.push(queryLimit, offset);

                const { rows: products } = await txClient.query(sqlQuery, queryParams);

                return {
                    products,
                    pagination: {
                        total_items: totalCount,
                        current_page: parseInt(page, 10),
                        total_pages: Math.ceil(totalCount / queryLimit),
                        limit: queryLimit
                    }
                };
            });

            return res.json({
                success: true,
                message: 'Products retrieved successfully.',
                data: result.products,
                pagination: result.pagination
            });

        } catch (error) {
            logger.error('Failed to retrieve catalog products', {
                tenant_id: req.tenantId,
                query_params: req.query,
                error: error.message,
                stack: error.stack
            });
            return res.status(500).json({
                success: false,
                error: 'InternalDatabaseError',
                message: 'Failed to query multi-tenant database product collections.'
            });
        }
    },

    /**
     * Get Single Product Details
     * 
     * Route: GET /api/v1/products/:id
     */
    getProductById: async (req, res) => {
        try {
            const { id } = req.params;
            const locale = req.query.locale || 'am';

            const product = await req.dbExecute(async (txClient) => {
                const { rows } = await txClient.query(`
                    SELECT 
                        id,
                        sku,
                        names->>$1 AS name,
                        descriptions->>$1 AS description,
                        names AS names_raw,
                        descriptions AS descriptions_raw,
                        specifications,
                        price,
                        currency,
                        inventory_count,
                        image_urls,
                        is_active,
                        category_id,
                        created_at
                    FROM products
                    WHERE id = $2
                `, [locale, id]);

                return rows[0] || null;
            });

            if (!product) {
                return res.status(404).json({
                    success: false,
                    error: 'ProductNotFound',
                    message: 'Requested product does not exist, or you lack permission to view it.'
                });
            }

            return res.json({
                success: true,
                data: product
            });

        } catch (error) {
            logger.error('Failed to retrieve detailed product information', {
                tenant_id: req.tenantId,
                product_id: id,
                error: error.message,
                stack: error.stack
            });
            return res.status(500).json({
                success: false,
                error: 'DatabaseQueryError',
                message: 'Failed to retrieve detailed product information.'
            });
        }
    },

    /**
     * Create Furniture Product
     * 
     * Route: POST /api/v1/products
     * Expected Body structure:
     * {
     *   "sku": "BZ-WNZ-C01",
     *   "names": { "en": "Wanza Gara L-Sofa", "am": "ዋንዛ ጋራ ኤል ሶፋ", "om": "Sofa Gara Wanza" },
     *   "descriptions": { "en": "Luxury hand-carved Wanza wood...", "am": "ከተመረጠ የዋንዛ እንጨት የተሰራ...", "om": "Kila hojjatame wood..." },
     *   "specifications": { "material": "Wanza", "warranty": "24 months", "dimensions": "300x200x85 cm" },
     *   "price": 85000.00,
     *   "currency": "ETB",
     *   "inventory_count": 5,
     *   "image_urls": ["https://storage.googleapis.com/bekansi/images/gara_sofa_1.jpg"],
     *   "category_id": "848972ac-9c60-4966-89b5-c053c7c460dc"
     * }
     */
    createProduct: async (req, res) => {
        try {
            const {
                sku,
                names,
                descriptions,
                specifications = {},
                price,
                currency = 'ETB',
                inventory_count = 0,
                image_urls = [],
                category_id
            } = req.body;

            // Enterprise payload guard checks
            if (!sku || !names || !names.en || !names.am || !price) {
                return res.status(400).json({
                    success: false,
                    error: 'ValidationError',
                    message: 'Missing mandatory fields. Products must have a SKU, pricing, and localized English + Amharic structural names.'
                });
            }

            // Execute within multi-tenant database session wrapper
            const newProduct = await req.dbExecute(async (txClient) => {
                // Check if SKU is already allocated within this organization
                const { rows: skuCheck } = await txClient.query(
                    'SELECT id FROM products WHERE sku = $1',
                    [sku]
                );

                if (skuCheck.length > 0) {
                    throw new Error('SKU_DUPLICATE');
                }

                // Verify category exists and matches tenant organization boundaries (RLS automatically enforces this)
                if (category_id) {
                    const { rows: categoryCheck } = await txClient.query(
                        'SELECT id FROM product_categories WHERE id = $1',
                        [category_id]
                    );
                    if (categoryCheck.length === 0) {
                        throw new Error('CATEGORY_NOT_FOUND');
                    }
                }

                // Insert modern record utilizing structural JSONB parameters explicitly
                const insertQuery = `
                    INSERT INTO products (
                        organization_id, sku, names, descriptions, specifications, 
                        price, currency, inventory_count, image_urls, category_id
                    ) VALUES (
                        $1, $2, $3::jsonb, $4::jsonb, $5::jsonb, $6, $7, $8, $9, $10
                    ) RETURNING *
                `;

                const values = [
                    req.tenantId, // Isolated tenant boundary
                    sku,
                    JSON.stringify(names),
                    JSON.stringify(descriptions || {}),
                    JSON.stringify(specifications),
                    parseFloat(price),
                    currency,
                    parseInt(inventory_count, 10),
                    image_urls,
                    category_id || null
                ];

                const { rows } = await txClient.query(insertQuery, values);
                return rows[0];
            });

            return res.status(211).json({
                success: true,
                message: 'Furniture product added to catalog successfully.',
                data: newProduct
            });

        } catch (error) {
            logger.error('Failed to create new catalog product', {
                tenant_id: req.tenantId,
                sku,
                category_id,
                error: error.message,
                stack: error.stack
            });

            if (error.message === 'SKU_DUPLICATE') {
                return res.status(409).json({
                    success: false,
                    error: 'DuplicateSKUTitle',
                    message: 'Products in your organization catalog cannot share identical Stock Keeping Unit (SKU) tags.'
                });
            }

            if (error.message === 'CATEGORY_NOT_FOUND') {
                return res.status(400).json({
                    success: false,
                    error: 'CategoryNotFound',
                    message: 'The selected product category was either deleted or belongs to another tenant catalog.'
                });
            }

            return res.status(500).json({
                success: false,
                error: 'DatabaseInsertionFailure',
                message: 'Database insert failed. Ensure attributes match multi-tenant schema formats.'
            });
        }
    },

    /**
     * Update Product Details
     * 
     * Route: PUT /api/v1/products/:id
     */
    updateProduct: async (req, res) => {
        try {
            const { id } = req.params;
            const {
                sku,
                names,
                descriptions,
                specifications,
                price,
                currency,
                inventory_count,
                image_urls,
                category_id,
                is_active
            } = req.body;

            const updatedProduct = await req.dbExecute(async (txClient) => {
                // Ensure target exists within organization bounds (RLS enforces)
                const { rows: currentProduct } = await txClient.query(
                    'SELECT * FROM products WHERE id = $1',
                    [id]
                );

                if (currentProduct.length === 0) {
                    throw new Error('PRODUCT_NOT_FOUND');
                }

                // If sku is updating, verify uniqueness
                if (sku && sku !== currentProduct[0].sku) {
                    const { rows: skuCheck } = await txClient.query(
                        'SELECT id FROM products WHERE sku = $1 AND id <> $2',
                        [sku, id]
                    );
                    if (skuCheck.length > 0) {
                        throw new Error('SKU_DUPLICATE');
                    }
                }

                // Prepare localized dynamic updates
                const mergedNames = names ? { ...currentProduct[0].names, ...names } : currentProduct[0].names;
                const mergedDescriptions = descriptions ? { ...currentProduct[0].descriptions, ...descriptions } : currentProduct[0].descriptions;
                const mergedSpecs = specifications ? { ...currentProduct[0].specifications, ...specifications } : currentProduct[0].specifications;

                const updateQuery = `
                    UPDATE products
                    SET 
                        sku = $1,
                        names = $2::jsonb,
                        descriptions = $3::jsonb,
                        specifications = $4::jsonb,
                        price = $5,
                        currency = $6,
                        inventory_count = $7,
                        image_urls = $8,
                        category_id = $9,
                        is_active = $10,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = $11
                    RETURNING *
                `;

                const values = [
                    sku || currentProduct[0].sku,
                    JSON.stringify(mergedNames),
                    JSON.stringify(mergedDescriptions),
                    JSON.stringify(mergedSpecs),
                    price !== undefined ? parseFloat(price) : currentProduct[0].price,
                    currency || currentProduct[0].currency,
                    inventory_count !== undefined ? parseInt(inventory_count, 10) : currentProduct[0].inventory_count,
                    image_urls || currentProduct[0].image_urls,
                    category_id !== undefined ? category_id : currentProduct[0].category_id,
                    is_active !== undefined ? is_active : currentProduct[0].is_active,
                    id
                ];

                const { rows } = await txClient.query(updateQuery, values);
                return rows[0];
            });

            return res.json({
                success: true,
                message: 'Furniture product catalog updated successfully.',
                data: updatedProduct
            });

        } catch (error) {
            logger.error('Failed to update catalog product', {
                tenant_id: req.tenantId,
                product_id: id,
                sku,
                error: error.message,
                stack: error.stack
            });

            if (error.message === 'PRODUCT_NOT_FOUND') {
                return res.status(404).json({
                    success: false,
                    error: 'ProductNotFound',
                    message: 'The requested product record does not exist or belongs to another tenant organization.'
                });
            }

            if (error.message === 'SKU_DUPLICATE') {
                return res.status(409).json({
                    success: false,
                    error: 'DuplicateSKUTitle',
                    message: 'Stock Keeping Unit (SKU) conflicts with another registered catalog product.'
                });
            }

            return res.status(500).json({
                success: false,
                error: 'DatabaseUpdateFailure',
                message: 'Catalog database modification pipeline broken.'
            });
        }
    },

    /**
     * Delete Furniture Product
     * 
     * Route: DELETE /api/v1/products/:id
     */
    deleteProduct: async (req, res) => {
        try {
            const { id } = req.params;

            await req.dbExecute(async (txClient) => {
                // Ensure target exists within organization block (RLS enforces)
                const { rows: currentCheck } = await txClient.query(
                    'SELECT id FROM products WHERE id = $1',
                    [id]
                );

                if (currentCheck.length === 0) {
                    throw new Error('PRODUCT_NOT_FOUND');
                }

                // Check for referential integrity: Check if product is bound to quotations
                const { rows: quoteCheck } = await txClient.query(
                    'SELECT id FROM quotation_items WHERE product_id = $1 LIMIT 1',
                    [id]
                );

                if (quoteCheck.length > 0) {
                    throw new Error('QUOTATION_DEPENDENCY_DETECTED');
                }

                // Safely remove catalog item
                await txClient.query('DELETE FROM products WHERE id = $1', [id]);
            });

            return res.json({
                success: true,
                message: 'Catalog product removed successfully.'
            });

        } catch (error) {
            logger.error('Failed to delete catalog product', {
                tenant_id: req.tenantId,
                product_id: id,
                error: error.message,
                stack: error.stack
            });

            if (error.message === 'PRODUCT_NOT_FOUND') {
                return res.status(404).json({
                    success: false,
                    error: 'ProductNotFound',
                    message: 'The requested product resource was not found.'
                });
            }

            if (error.message === 'QUOTATION_DEPENDENCY_DETECTED') {
                return res.status(422).json({
                    success: false,
                    error: 'ActiveReferentialDependency',
                    message: 'Cannot delete products tied to active customer quotation objects. Try setting \'is_active = FALSE\' to archive instead.'
                });
            }

            return res.status(500).json({
                success: false,
                error: 'DatabaseUpdateFailure',
                message: 'Failed to executing transaction instructions in catalog DB pool.'
            });
        }
    }
};

/**
 * ============================================================================
 * EXPORT ROUTER CONFIGURATION
 * ============================================================================
 */

// Route Middlewares: Apply authentication context first, then inject scoped DB transaction engine
router.use(tenantAuthenticator);
router.use(injectScopedDbClient(require('./config/dbPool'))); // Assuming system configuration holds db pool exports

// HTTP Gateway endpoints mapped directly to tenant isolated actions
router.get('/products', CatalogController.getProducts);
router.get('/products/:id', CatalogController.getProductById);
router.post('/products', CatalogController.createProduct);
router.put('/products/:id', CatalogController.updateProduct);
router.delete('/products/:id', CatalogController.deleteProduct);

module.exports = router;

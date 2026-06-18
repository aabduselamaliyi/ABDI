/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * enterprise-grade tenant isolation middleware (Node.js / Express)
 * ============================================================================
 *
 * This middleware authenticates users via JWT, extracts their tenant ID (organization_id),
 * and configures PostgreSQL database sessions so that Row-Level Security (RLS)
 * is safely enforced.
 *
 * Requirements addressed:
 * 1. Extract Organization/Tenant context from JSON Web Tokens safely.
 * 2. Handle JWT expiration, presence, structure, and signature validation.
 * 3. Scope Database connections/transactions using "SET LOCAL app.current_tenant_id"
 *    to align with RLS policies declared in our PostgreSQL DB schema.
 * 4. Supply clean integration hooks for 'pg' (node-postgres) Pools and Clients.
 */

const jwt = require('jsonwebtoken');
const logger = require('./logger');

// Ensure JWT Secret is present in environment variables
const JWT_SECRET = process.env.JWT_SECRET || 'bekansi_super_secret_enterprise_signing_key_change_me';

/**
 * Express Middleware to Authenticate and Bind Tenant Context
 * 
 * Extracts Bearer Token, validates signature, ensures tenant active flag,
 * and attaches authenticated fields to req.user and req.tenantId.
 */
const tenantAuthenticator = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                error: 'Unauthenticated',
                message: 'Authorization header is missing or malformed. Expected Bearer Token.'
            });
        }

        const token = authHeader.split(' ')[1];
        
        // Verify JWT token signature and expiration
        jwt.verify(token, JWT_SECRET, (err, decoded) => {
            if (err) {
                const isExpired = err.name === 'TokenExpiredError';
                logger.warn('SaaS JWT Verification Failed', {
                    error_type: err.name,
                    message: err.message,
                    token_snippet: token.substring(0, 15) + '...',
                    is_expired: isExpired
                });
                return res.status(401).json({
                    error: isExpired ? 'TokenExpired' : 'InvalidToken',
                    message: isExpired ? 'Your session has expired. Please log in again.' : 'Invalid token signature or payload.'
                });
            }

            // High-security check: ensure mandatory claims exist
            if (!decoded.organization_id || !decoded.user_id || !decoded.role) {
                logger.error('SaaS Access Denied: Missing Tenant Claims in token', {
                    user_id: decoded?.user_id || null,
                    has_org: !!decoded?.organization_id,
                    has_role: !!decoded?.role
                });
                return res.status(403).json({
                    error: 'ForbiddenClaims',
                    message: 'Token does not contain required SaaS tenant attributes (organization_id, user_id, role).'
                });
            }

            // Bind tenant identity to the incoming Express Request object
            req.user = {
                id: decoded.user_id,
                email: decoded.email,
                role: decoded.role,
                organization_id: decoded.organization_id
            };
            req.tenantId = decoded.organization_id;

            logger.info('Authorized SaaS session bound to request context', {
                user_id: req.user.id,
                tenant_id: req.tenantId,
                role: req.user.role
            });

            return next();
        });

    } catch (error) {
        logger.error('Unhandled SaaS tenant authenticator boundary error', {
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            error: 'InternalServerError',
            message: 'An error occurred during authentication.'
        });
    }
};

/**
 * PostgreSQL Multi-Tenant Query Wrapper
 * 
 * Executes database operations within a scoped transaction where
 * "SET LOCAL app.current_tenant_id" is run before the real queries.
 * This guarantees that even if a developer forgets to append
 * "WHERE organization_id = ..." to the SQL, Postgres RLS will drop
 * records belonging to other tentants automatically.
 * 
 * @param {import('pg').Pool} dbPool The PG database pool connection
 * @param {string} tenantId The organization_id UUID to scope to
 * @param {function(import('pg').PoolClient): Promise<any>} transactionWork Callback containing your database queries
 * @returns {Promise<any>} Response output from callback
 */
const executeScopedQuery = async (dbPool, tenantId, transactionWork) => {
    const transactionStartTime = Date.now();
    const client = await dbPool.connect();

    // Use a JS proxy to automatically catch, log, and time queries run inside the transaction
    const clientProxy = new Proxy(client, {
        get(target, prop) {
            if (prop === 'query') {
                return async (...args) => {
                    const queryStartTime = Date.now();
                    const queryArg = args[0];
                    let sqlQuery = '';
                    let queryParams = [];

                    if (typeof queryArg === 'string') {
                        sqlQuery = queryArg;
                        queryParams = args[1] || [];
                    } else if (queryArg && typeof queryArg === 'object') {
                        sqlQuery = queryArg.text || '';
                        queryParams = queryArg.values || [];
                    }

                    // Clean the spacing of SQL query text for structured logs readability
                    const sqlMetricText = sqlQuery.replace(/\s+/g, ' ').trim();

                    try {
                        const result = await target.query(...args);
                        const duration = Date.now() - queryStartTime;

                        // Emit success metrics
                        logger.info('Postgres RLS Scoped Query Executed', {
                            tenant_id: tenantId,
                            sql: sqlMetricText,
                            parameters_count: queryParams.length,
                            duration_ms: duration,
                            rows_returned: result.rows ? result.rows.length : 0
                        });

                        return result;
                    } catch (err) {
                        const duration = Date.now() - queryStartTime;

                        // Emit failure logs
                        logger.error('Postgres RLS Scoped Query Failed', {
                            tenant_id: tenantId,
                            sql: sqlMetricText,
                            duration_ms: duration,
                            error: err.message,
                            stack: err.stack
                        });

                        throw err;
                    }
                };
            }
            return target[prop];
        }
    });

    try {
        logger.info('Starting isolated tenant PostgreSQL transaction context', { tenant_id: tenantId });

        // Begin a separate transaction to secure the local session variable context
        await client.query('BEGIN');

        // Set the current_tenant_id value in the current session transaction scope.
        // Third argument 'true' ensures this parameter is local to the current transaction.
        await client.query({
            text: 'SELECT set_config($1, $2, true)',
            values: ['app.current_tenant_id', tenantId]
        });

        // Run client work using our instrumented proxy connection
        const result = await transactionWork(clientProxy);

        // Commit and persist changes safely
        await client.query('COMMIT');

        const totalDuration = Date.now() - transactionStartTime;
        logger.info('Database tenant transaction committed successfully', {
            tenant_id: tenantId,
            total_duration_ms: totalDuration
        });

        return result;

    } catch (error) {
        // Rollback on any failure to maintain absolute transaction atomicity
        await client.query('ROLLBACK');

        const totalDuration = Date.now() - transactionStartTime;
        logger.error('Database transaction rollback triggered by execution failure', {
            tenant_id: tenantId,
            total_duration_ms: totalDuration,
            error: error.message,
            stack: error.stack
        });

        throw error;
    } finally {
        // Release client back to PG Pool
        client.release();
    }
};

/**
 * Controller integration helper
 * 
 * Generates ready-to-use tenant-scoped PG executor for route controllers.
 * 
 * Example Route Usage:
 * 
 * router.get('/products', tenantAuthenticator, async (req, res) => {
 *     try {
 *         const products = await req.dbExecute(async (txClient) => {
 *             // Automatically gets filtered for req.tenantId only!
 *             const { rows } = await txClient.query('SELECT * FROM products');
 *             return rows;
 *         });
 *         res.json({ success: true, count: products.length, data: products });
 *     } catch (err) {
 *         res.status(500).json({ error: err.message });
 *     }
 * });
 */
/**
 * Centralized Request & Response Logging Middleware
 * 
 * Hooks into the Express lifecycle to log all incoming HTTP transactions and
 * stream completed states with high precision response duration metrics.
 */
const requestResponseLogger = (req, res, next) => {
    const startTime = Date.now();
    const { method, originalUrl, ip } = req;
    const userAgent = req.get('user-agent') || 'Unknown';

    // Log the incoming request immediately with basic parameters
    logger.info(`HTTP Incoming Request: ${method} ${originalUrl}`, {
        method,
        url: originalUrl,
        ip,
        user_agent: userAgent,
        query: req.query || {}
    });

    // Monitor HTTP transaction completion events to record response codes and exact processing times
    res.on('finish', () => {
        const duration = Date.now() - startTime;
        const statusCode = res.statusCode;
        const tenantId = req.tenantId || null;
        const userId = req.user?.id || null;

        const responseMetadata = {
            method,
            url: originalUrl,
            status_code: statusCode,
            duration_ms: duration,
            tenant_id: tenantId,
            user_id: userId
        };

        if (statusCode >= 500) {
            logger.error(`HTTP Response Failed (Server Error): ${method} ${originalUrl} -> ${statusCode}`, responseMetadata);
        } else if (statusCode >= 400) {
            logger.warn(`HTTP Response Failure (Client Error): ${method} ${originalUrl} -> ${statusCode}`, responseMetadata);
        } else {
            logger.info(`HTTP Response Completed: ${method} ${originalUrl} -> ${statusCode}`, responseMetadata);
        }
    });

    return next();
};

const injectScopedDbClient = (dbPool) => {
    return (req, res, next) => {
        if (!req.tenantId) {
            return res.status(400).json({
                error: 'MissingTenantContext',
                message: 'Database query execution requested but tenant ID was not mounted.'
            });
        }

        // Attach helper method to execution pipeline
        req.dbExecute = (transactionWork) => executeScopedQuery(dbPool, req.tenantId, transactionWork);
        return next();
    };
};

module.exports = {
    tenantAuthenticator,
    executeScopedQuery,
    injectScopedDbClient,
    requestResponseLogger
};

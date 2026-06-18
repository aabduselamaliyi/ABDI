/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * PRODUCTION-GRADE MULTI-TENANT ENTERPRISE EXPRESS SERVER
 * ============================================================================
 *
 * This file serves as the main application entry point. It orchestrates the
 * full backend stack:
 * 1. Global security policies (Helmet, CORS, Rate Limiters).
 * 2. Winston Observability & Request timing tracers.
 * 3. Mounts multi-tenant RLS scoped REST API routers.
 * 4. Captures server exception crash safety bounds.
 */

require('dotenv').config();
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const path = require('path');

const logger = require('./logger');
const dbPool = require('./config/dbPool');
const { requestResponseLogger } = require('./tenantMiddleware');

// Import versioned SaaS controllers and routers
const authRouter = require('./routes/auth');
const catalogRouter = require('./catalogRouter'); // Already structured in root directory
const leadsRouter = require('./routes/leads');
const customersRouter = require('./routes/customers');
const quotationsRouter = require('./routes/quotations');
const conversationsRouter = require('./routes/conversations');

const app = express();
const PORT = process.env.PORT || 3000;

// ============================================================================
// SECURITY LAYER (Helmet, CORS & Global rate limiters)
// ============================================================================

app.use(helmet({
    contentSecurityPolicy: process.env.NODE_ENV === 'production',
    crossOriginEmbedderPolicy: process.env.NODE_ENV === 'production'
}));

const corsOptions = {
    origin: process.env.ALLOWED_ORIGINS 
        ? process.env.ALLOWED_ORIGINS.split(',') 
        : ['http://localhost:3000', 'http://localhost:5173', 'https://ai.studio'],
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
    credentials: true,
    maxAge: 86400 // Cache preflight connection requests (24 hours)
};
app.use(cors(corsOptions));

// Safe global rate limiter against DDoS exploits (100 requests per single client IP per 10 mins)
const globalRateLimiter = rateLimit({
    windowMs: 10 * 60 * 1000,
    max: parseInt(process.env.RATE_LIMIT_MAX || '100', 10),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: {
        success: false,
        error: 'TooManyRequests',
        message: 'Rate limit exceeded. Please wait 10 minutes before retrying.'
    },
    handler: (req, res, next, options) => {
        logger.warn(`DDoS rate limit trigger warning`, { ip: req.ip, url: req.originalUrl });
        res.status(429).json(options.message);
    }
});
app.use('/api/', globalRateLimiter);

// Parse JSON and URL encoded raw payloads with size caps
app.use(express.json({ limit: '5mb' }));
app.use(express.urlencoded({ extended: true, limit: '2mb' }));

// Apply Winston Request/Response structured telemetry trace hooks
app.use(requestResponseLogger);

// ============================================================================
// VERSIONED SAAS RUNTIME ENDPOINTS
// ============================================================================

// Public check route for uptime statistics and DevOps monitors
app.get('/health', async (req, res) => {
    try {
        // Run light DB health verify query
        const dbVerify = await dbPool.query('SELECT NOW()');
        return res.json({
            status: 'healthy',
            timestamp: new Date().toISOString(),
            uptime_seconds: Math.floor(process.uptime()),
            connections: {
                database_connected: !!dbVerify.rows[0],
                total_pool_clients: dbPool.totalCount,
                idle_pool_clients: dbPool.idleCount
            }
        });
    } catch (err) {
        logger.error('SaaS Infrastructure Health check assertion failed', {
            error: err.message,
            stack: err.stack
        });
        return res.status(503).json({
            status: 'unhealthy',
            error: err.message,
            timestamp: new Date().toISOString()
        });
    }
});

// Bind route scopes
app.use('/api/v1/auth', authRouter);
app.use('/api/v1', catalogRouter); // Houses product catalog / products routes safely
app.use('/api/v1/leads', leadsRouter);
app.use('/api/v1/customers', customersRouter);
app.use('/api/v1/quotations', quotationsRouter);
app.use('/api/v1/conversations', conversationsRouter);

// Set root welcome redirect index
app.get('/', (req, res) => {
    res.json({
        service: 'Bekansi AI SaaS CRM Enterprise API Platform',
        version: '1.0.0',
        author: 'Bekansi AI Ethiopian Engineering Division',
        environment: process.env.NODE_ENV || 'development'
    });
});

// Handle catch-all 404 API routes
app.use((req, res, next) => {
    res.status(404).json({
        success: false,
        error: 'RouteNotFound',
        message: `Resource endpoint ${req.method} ${req.originalUrl} does not exist.`
    });
});

// ============================================================================
// CENTRAL OBSERVABILITY ERROR-BOUNDARY HANDLER (Winston Integration)
// ============================================================================

app.use((err, req, res, next) => {
    const errorId = `ERR-${Math.floor(100000 + Math.random() * 900000)}`;
    const statusCode = err.status || err.statusCode || 500;

    // Log the event details with full traceback details to Winston
    logger.error(`Express Execution Boundary caught unhandled exception: ${err.message}`, {
        error_id: errorId,
        status_code: statusCode,
        method: req.method,
        url: req.originalUrl,
        ip: req.ip,
        user_id: req.user?.id || null,
        tenant_id: req.tenantId || null,
        stack: err.stack
    });

    return res.status(statusCode).json({
        success: false,
        error_id: errorId,
        error: statusCode === 500 ? 'InternalServerError' : err.name || 'UnexpectedApplicationError',
        message: statusCode === 500
            ? 'A secure backend error occurred. Our engineers are investigating. Trace ID: ' + errorId
            : err.message
    });
});

// ============================================================================
// INSTANT DEPLOYMENT STARTUP SEED
// ============================================================================

const server = app.listen(PORT, '0.0.0.0', () => {
    logger.info(`======================================================================`);
    logger.info(`  BEKANSI AI ENTERPRISE SaaS ENGINE IS DEPLOYED                          `);
    logger.info(`  Environment: ${process.env.NODE_ENV || 'development'}                `);
    logger.info(`  API Gateway: http://0.0.0.0:${PORT}                                  `);
    logger.info(`======================================================================`);
});

// Handle sudden VM or Container terminations gracefully
const handleProcessTermination = async (signal) => {
    logger.warn(`Signal ${signal} intercepted. Commencing graceful process termination...`);
    
    server.close(async () => {
        logger.info('HTTP server handles closed. Terminating physical active pools...');
        await dbPool.shutdownDbPool();
        logger.info('Environment variables flush completed. Process exiting safely.');
        process.exit(0);
    });

    // Enforce instant kill exit after 5 seconds
    setTimeout(() => {
        logger.error('Graceful shutdown timeout exceeded. Enforcing instant kill exit.');
        process.exit(1);
    }, 5000);
};

process.on('SIGTERM', () => handleProcessTermination('SIGTERM'));
process.on('SIGINT', () => handleProcessTermination('SIGINT'));

module.exports = app;

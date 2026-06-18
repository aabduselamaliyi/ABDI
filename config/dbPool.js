/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * PRODUCTION-GRADE POSTGRESQL DATABASE POOL (node-postgres)
 * ============================================================================
 *
 * Configures and exports a production-ready PostgreSQL connection pool (`pg.Pool`).
 * Leverages structured logging to capture connection acquisitions, terminations,
 * and pool health parameters.
 */

const { Pool } = require('pg');
const logger = require('../logger');

// Ensure database URL is provided via environment variables, with a sensible SaaS default
const DATABASE_URL = process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/bekansi_crm';

const poolConfig = {
    connectionString: DATABASE_URL,
    // Max number of clients inside the pool
    max: parseInt(process.env.PG_POOL_MAX || '20', 10),
    // Idle client eviction threshold (30 seconds)
    idleTimeoutMillis: parseInt(process.env.PG_IDLE_TIMEOUT || '30000', 10),
    // Maximum time to wait for a database client before throwing an error (2 seconds)
    connectionTimeoutMillis: parseInt(process.env.PG_CONN_TIMEOUT || '2000', 10),
    // Enable SSL configuration for production cloud DBMS systems (e.g. Supabase, AWS RDS)
    ssl: process.env.NODE_ENV === 'production' && !process.env.PG_DISABLE_SSL
        ? { rejectUnauthorized: false }
        : false
};

logger.info('Initializing PostgreSQL connection pool manager', {
    pool_max_clients: poolConfig.max,
    idle_timeout_ms: poolConfig.idleTimeoutMillis,
    conn_timeout_ms: poolConfig.connectionTimeoutMillis,
    ssl_enabled: !!poolConfig.ssl
});

const pool = new Pool(poolConfig);

// Monitor connection pool active states
pool.on('connect', (client) => {
    logger.info('New database client connection established with Postgres Pool', {
        active_connections: pool.totalCount,
        idle_connections: pool.idleCount
    });
});

pool.on('error', (err, client) => {
    logger.error('Unexpected database client error in PostgreSQL Pool', {
        error: err.message,
        stack: err.stack,
        pool_total: pool.totalCount,
        pool_idle: pool.idleCount
    });
});

pool.on('acquire', (client) => {
    if (process.env.LOG_LEVEL === 'debug') {
        logger.debug('Database client acquired from postgres transaction pool');
    }
});

// Graceful pool shutdown logic
const shutdownDbPool = async () => {
    logger.info('Gracefully closing PostgreSQL connection pool...');
    try {
        await pool.end();
        logger.info('PostgreSQL pool shut down completed.');
    } catch (err) {
        logger.error('Failed to gracefully close database connections', {
            error: err.message,
            stack: err.stack
        });
    }
};

pool.shutdownDbPool = shutdownDbPool;

module.exports = pool;

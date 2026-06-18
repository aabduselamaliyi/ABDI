/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * ENTERPRISE MULTI-TENANT AUTHENTICATION ROUTER (JWT + RBAC + ONBOARDING)
 * ============================================================================
 *
 * Provides routes for user authentication, token validation, and multi-tenant
 * organization onboarding. Ensures that user authentication queries operate
 * within isolated boundaries.
 */

const express = require('express');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');
const logger = require('../logger');
const dbPool = require('../config/dbPool');
const { executeScopedQuery } = require('../tenantMiddleware');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET || 'bekansi_super_secret_enterprise_signing_key_change_me';
const JWT_EXPIRY = process.env.JWT_EXPIRY || '24h';

/**
 * Endpoint: POST /api/v1/auth/onboard
 * Role: Public (Tenant Registration)
 * Description: Registers a new Organization (Tenant) and seeds their initial Tenant Admin.
 */
router.post('/onboard', async (req, res) => {
    const { org_name, subdomain, admin_email, admin_password, first_name, last_name, phone_number } = req.body;

    if (!org_name || !subdomain || !admin_email || !admin_password || !first_name || !last_name) {
        return res.status(400).json({
            success: false,
            error: 'ValidationFailed',
            message: 'All core parameters (org_name, subdomain, admin_email, admin_password, first_name, last_name) are required for tenant onboarding.'
        });
    }

    const client = await dbPool.connect();

    try {
        await client.query('BEGIN');

        // 1. Check if subdomain is already registered
        const { rows: existingOrg } = await client.query(
            'SELECT id FROM organizations WHERE subdomain = $1',
            [subdomain.toLowerCase().trim()]
        );
        if (existingOrg.length > 0) {
            await client.query('ROLLBACK');
            return res.status(409).json({
                success: false,
                error: 'SubdomainReserved',
                message: `The subdomain "${subdomain}" is already registered. Please choose another one.`
            });
        }

        // 2. Create the Organization record
        const { rows: orgRows } = await client.query(`
            INSERT INTO organizations (name, subdomain, billing_plan)
            VALUES ($1, $2, $3)
            RETURNING id, name, subdomain, is_active
        `, [org_name.trim(), subdomain.toLowerCase().trim(), 'growth']);

        const organizationId = orgRows[0].id;

        // 3. Encrypt the Administrator's password credentials
        const salt = await bcrypt.genSalt(12);
        const passwordHash = await bcrypt.hash(admin_password, salt);

        // 4. Create the User record (Seed as 'tenant_admin' role)
        // Since Postgres RLS is enabled, we scope the tenant_id in the connection local session variable
        // to pass downstream CHECK constraints, or run this initialization before user-level policies trigger.
        await client.query({
            text: 'SELECT set_config($1, $2, true)',
            values: ['app.current_tenant_id', organizationId]
        });

        const { rows: userRows } = await client.query(`
            INSERT INTO users (organization_id, email, password_hash, first_name, last_name, role, phone_number)
            VALUES ($1, $2, $3, $4, $5, 'tenant_admin'::user_role, $6)
            RETURNING id, email, first_name, last_name, role, is_active
        `, [
            organizationId,
            admin_email.toLowerCase().trim(),
            passwordHash,
            first_name.trim(),
            last_name.trim(),
            phone_number || null
        ]);

        await client.query('COMMIT');

        logger.info('Successfully onboarded new organization tenant', {
            organization_id: organizationId,
            organization_name: org_name,
            subdomain,
            admin_user_id: userRows[0].id
        });

        return res.status(201).json({
            success: true,
            message: 'Organization onboarded and localized successfully. Please authenticate to continue.',
            data: {
                organization: orgRows[0],
                administrator: userRows[0]
            }
        });

    } catch (error) {
        await client.query('ROLLBACK');
        logger.error('Onboarding transaction abort caused by exception', {
            org_name,
            subdomain,
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            success: false,
            error: 'OnboardingFailed',
            message: 'Failed to onboard new tenant due to an internal server error.'
        });
    } finally {
        client.release();
    }
});

/**
 * Endpoint: POST /api/v1/auth/login
 * Role: Public (User Login)
 * Description: Validates credentials, checks subdomain/tenant state, and returns signed JWT.
 */
router.post('/login', async (req, res) => {
    const { email, password, subdomain } = req.body;

    if (!email || !password || !subdomain) {
        return res.status(400).json({
            success: false,
            error: 'MissingCredentials',
            message: 'Email, password, and tenant subdomain identifiers are mandatory.'
        });
    }

    try {
        // Find organization by subdomain
        const { rows: orgs } = await dbPool.query(
            'SELECT id, name, is_active FROM organizations WHERE subdomain = $1',
            [subdomain.toLowerCase().trim()]
        );

        if (orgs.length === 0) {
            logger.warn('Auth Failure: Invalid tenant subdomain', { subdomain });
            return res.status(401).json({
                success: false,
                error: 'AuthenticationFailed',
                message: 'Invalid subdomain or email credentials.'
            });
        }

        const org = orgs[0];
        if (!org.is_active) {
            logger.warn('Auth Failure: Attempt to access deactivated tenant', { organization_id: org.id });
            return res.status(403).json({
                success: false,
                error: 'OrganizationLocked',
                message: 'Your organization account is suspended. Please contact customer support.'
            });
        }

        // Match user credentials by scoping connection using organizational ID (enforcing RLS during selection)
        const user = await executeScopedQuery(dbPool, org.id, async (txClient) => {
            const { rows: u } = await txClient.query(
                'SELECT id, email, password_hash, first_name, last_name, role, is_active FROM users WHERE email = $1',
                [email.toLowerCase().trim()]
            );
            return u[0] || null;
        });

        if (!user) {
            logger.warn('Auth Failure: Invalid user credentials', { email, organization_id: org.id });
            return res.status(401).json({
                success: false,
                error: 'AuthenticationFailed',
                message: 'Invalid subdomain or email credentials.'
            });
        }

        if (!user.is_active) {
            return res.status(403).json({
                success: false,
                error: 'UserDeactivated',
                message: 'Your personal user profile has been suspended by an administrator.'
            });
        }

        // Validate password hash with bcrypt
        const isPasswordCorrect = await bcrypt.compare(password, user.password_hash);
        if (!isPasswordCorrect) {
            logger.warn('Auth Failure: Incorrect password supplied', { email, user_id: user.id });
            return res.status(401).json({
                success: false,
                error: 'AuthenticationFailed',
                message: 'Invalid subdomain or email credentials.'
            });
        }

        // Issue multi-tenant JWT payload
        const jwtPayload = {
            user_id: user.id,
            email: user.email,
            role: user.role,
            organization_id: org.id
        };

        const token = jwt.sign(jwtPayload, JWT_SECRET, { expiresIn: JWT_EXPIRY });

        logger.info('User session generated successfully', {
            user_id: user.id,
            tenant_id: org.id,
            role: user.role
        });

        return res.json({
            success: true,
            message: 'Authentication successful.',
            token,
            user: {
                id: user.id,
                email: user.email,
                first_name: user.first_name,
                last_name: user.last_name,
                role: user.role
            },
            organization: {
                id: org.id,
                name: org.name,
                subdomain: org.subdomain
            }
        });

    } catch (error) {
        logger.error('Login routing block encountered unhandled error', {
            email,
            subdomain,
            error: error.message,
            stack: error.stack
        });
        return res.status(500).json({
            success: false,
            error: 'ServerAuthFailure',
            message: 'Authentication process halted due to a database context error.'
        });
    }
});

module.exports = router;

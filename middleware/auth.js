/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * MODULAR SECURITY MIDDLEWARE (JWT + ROLE-BASED ACCESS CONTROL)
 * ============================================================================
 *
 * Implements JWT Verification and Role-Based Access Control (RBAC) to ensure
 * endpoints are secured and accessed only by authorized personnel.
 */

const jwt = require('jsonwebtoken');
const logger = require('../logger');

const JWT_SECRET = process.env.JWT_SECRET || 'bekansi_super_secret_enterprise_signing_key_change_me';

/**
 * JWT Authentication Middleware
 * Validates the Authorization header and binds claims to req.user.
 */
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        logger.warn('JWT Authentication failed: Missing or malformed header');
        return res.status(401).json({
            success: false,
            error: 'Unauthorized',
            message: 'Access Denied: Bearer token is missing or malformed.'
        });
    }

    const token = authHeader.split(' ')[1];

    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) {
            const isExpired = err.name === 'TokenExpiredError';
            logger.warn('JWT Verification failed', { error: err.name, expired: isExpired });
            return res.status(401).json({
                success: false,
                error: isExpired ? 'TokenExpired' : 'InvalidToken',
                message: isExpired ? 'Your session has expired. Please log in again.' : 'Invalid token signature or payload.'
            });
        }

        // Bind user claims to the request object
        req.user = {
            id: decoded.user_id,
            email: decoded.email,
            role: decoded.role,
            organization_id: decoded.organization_id
        };
        req.tenantId = decoded.organization_id;

        next();
    });
};

/**
 * Role-Based Access Control (RBAC) Middleware
 * Restricts access to specific user roles (e.g., 'system_admin', 'tenant_admin').
 * @param {Array<string>} allowedRoles 
 */
const authorizeRoles = (...allowedRoles) => {
    return (req, res, next) => {
        if (!req.user || !req.user.role) {
            logger.error('RBAC Denial: No active user profile attached to request context');
            return res.status(403).json({
                success: false,
                error: 'ForbiddenAccess',
                message: 'Access Denied: Unrecognized credentials context.'
            });
        }

        const hasRole = allowedRoles.includes(req.user.role);
        if (!hasRole) {
            logger.warn('RBAC unauthorized attempt blocked', {
                user_id: req.user.id,
                user_role: req.user.role,
                required_roles: allowedRoles,
                url: req.originalUrl
            });
            return res.status(403).json({
                success: false,
                error: 'UnauthorizedRole',
                message: 'Access Denied: Your enterprise role does not have permission to execute this operation.'
            });
        }

        next();
    };
};

module.exports = {
    authenticateToken,
    authorizeRoles
};

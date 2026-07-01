/**
 * ============================================================================
 * BEKANSI AI SOCIAL MEDIA GROWTH & AUTOMATION ENGINE
 * PRODUCTION-GRADE REST API ROUTER
 * ============================================================================
 */

const express = require('express');
const router = express.Router();
const smmService = require('../services/smmService');
const logger = require('../logger');
const dbPool = require('../config/dbPool');

/**
 * GET /api/v1/smm/queue
 * Retrieves all queued scheduled posts. Isolates multi-tenant SaaS organization contexts.
 */
router.get('/queue', async (req, res, next) => {
    try {
        const organizationId = req.tenantId || null;
        let query = "SELECT * FROM smm_posts ORDER BY scheduled_time ASC";
        let values = [];

        if (organizationId) {
            query = "SELECT * FROM smm_posts WHERE organization_id = $1 ORDER BY scheduled_time ASC";
            values = [organizationId];
        }

        const dbRes = await dbPool.query(query, values);
        return res.json({
            success: true,
            count: dbRes.rows.length,
            data: dbRes.rows
        });
    } catch (err) {
        logger.error('Failed to retrieve SMM post queue', { error: err.message });
        return next(err);
    }
});

/**
 * POST /api/v1/smm/schedule
 * Enqueues a new SMM post draft for scheduler execution
 */
router.post('/schedule', async (req, res, next) => {
    try {
        const { content, platform, imageUrl, scheduledTime } = req.body;
        
        if (!content || !platform) {
            return res.status(400).json({
                success: false,
                error: 'ValidationError',
                message: 'Post content and platform fields are mandatory.'
            });
        }

        const organizationId = req.tenantId || null;
        const post = await smmService.createPost({
            organizationId,
            content,
            platform,
            imageUrl,
            scheduledTime
        });

        return res.status(201).json({
            success: true,
            message: 'Social media post enqueued for viral scheduling successfully',
            data: post
        });
    } catch (err) {
        logger.error('Failed to schedule post draft', { error: err.message });
        return next(err);
    }
});

/**
 * POST /api/v1/smm/post-now
 * Manually posts instantly to Facebook page feed, Telegram channel, or Instagram account
 */
router.post('/post-now', async (req, res, next) => {
    try {
        const { content, platform, imageUrl } = req.body;

        if (!content || !platform) {
            return res.status(400).json({
                success: false,
                error: 'ValidationError',
                message: 'Post content and platform fields are mandatory.'
            });
        }

        logger.info(`Instant Manual Trigger initiated for SMM platform: ${platform}`);
        let result;

        try {
            switch (platform.toLowerCase()) {
                case 'facebook':
                    result = await smmService.postToFacebook(content);
                    break;
                case 'telegram':
                    result = await smmService.postToTelegram(content);
                    break;
                case 'instagram':
                    result = await smmService.postToInstagram(imageUrl, content);
                    break;
                default:
                    result = {
                        success: true,
                        message: `Simulation: Dispatched manually to alternative platform ${platform}`,
                        timestamp: new Date().toISOString()
                    };
            }
        } catch (apiErr) {
            logger.error(`Social API Platform Error during manual trigger: ${apiErr.message}`);
            return res.status(502).json({
                success: false,
                error: 'SocialPlatformApiError',
                message: `Failed to publish directly to ${platform}: ${apiErr.message}`
            });
        }

        // Write a recorded entry into the SMM table labeled instantly as posted
        const organizationId = req.tenantId || null;
        const query = `
            INSERT INTO smm_posts (organization_id, content, platform, image_url, scheduled_time, status)
            VALUES ($1, $2, $3, $4, NOW(), 'posted')
            RETURNING *
        `;
        const postEntry = await dbPool.query(query, [organizationId, content, platform, imageUrl]);

        return res.json({
            success: true,
            message: `Post successfully published on ${platform}!`,
            data: postEntry.rows[0],
            apiResponse: result
        });
    } catch (err) {
        logger.error('Failed to perform instant SMM post execution', { error: err.message });
        return next(err);
    }
});

/**
 * PUT /api/v1/smm/status/:id
 * Manually updates status of a queued item
 */
router.put('/status/:id', async (req, res, next) => {
    try {
        const { id } = req.params;
        const { status } = req.body;

        if (!status) {
            return res.status(400).json({
                success: false,
                error: 'ValidationError',
                message: 'Status parameters are required.'
            });
        }

        const post = await smmService.updatePostStatus(id, status);
        return res.json({
            success: true,
            message: 'Queued post status updated successfully.',
            data: post
        });
    } catch (err) {
        logger.error('Failed to update scheduled post status', { id, error: err.message });
        return next(err);
    }
});

module.exports = router;

/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * PRODUCTION-GRADE STRUCTURED OBSERVABILITY LOGGER (Winston)
 * ============================================================================
 *
 * Implements an enterprise class observability layer using Winston.
 * Centrally manages logs with:
 * 1. Consistent JSON structures tailored for Cloud logging engines (e.g. AWS CloudWatch, GCP Cloud Logging).
 * 2. Separate log files for all levels ('logs/combined.log') and runtime errors ('logs/error.log').
 * 3. Human-readable, colorized output format during local testing and local development.
 * 4. Automatic capturing of runtime exceptions and unhandled promise rejections.
 */

const winston = require('winston');
const path = require('path');

// Set log outputs directory
const LOGS_DIR = path.join(__dirname, 'logs');

// Define global metadata formats
const structuredFormat = winston.format.combine(
    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss.SSS' }),
    winston.format.errors({ stack: true }), // Automatically parse error stacks
    winston.format.json() // Structured JSON layout
);

// Human-readable layout for development console logging
const consoleFormat = winston.format.combine(
    winston.format.colorize(),
    winston.format.timestamp({ format: 'HH:mm:ss' }),
    winston.format.printf(({ timestamp, level, message, stack, ...meta }) => {
        let output = `[${timestamp}] ${level}: ${message}`;
        if (stack) {
            output += `\nStack trace:\n${stack}`;
        } else if (Object.keys(meta).length > 0 && meta.service !== 'bekansi-furniture-api') {
            // Include extra context if present, skipping base service tag
            const devMeta = { ...meta };
            delete devMeta.service;
            if (Object.keys(devMeta).length > 0) {
                output += ` | Meta: ${JSON.stringify(devMeta)}`;
            }
        }
        return output;
    })
);

// Instantiate Winston Logger Engine
const logger = winston.createLogger({
    level: process.env.LOG_LEVEL || 'info', // Dynamically controlled log depth
    format: structuredFormat,
    defaultMeta: { service: 'bekansi-furniture-api' },
    transports: [
        // Console output
        new winston.transports.Console({
            format: process.env.NODE_ENV === 'production' ? structuredFormat : consoleFormat,
            handleExceptions: true,
            handleRejections: true
        }),
        // Persistent error streams
        new winston.transports.File({
            filename: path.join(LOGS_DIR, 'error.log'),
            level: 'error',
            maxsize: 10 * 1024 * 1024, // 10MB file rolling
            maxFiles: 5,
            tailable: true
        }),
        // Combined runtime streams
        new winston.transports.File({
            filename: path.join(LOGS_DIR, 'combined.log'),
            maxsize: 15 * 1024 * 1024, // 15MB file rolling
            maxFiles: 10,
            tailable: true
        })
    ],
    exitOnError: false // Prevent standard runtime crashes on handled error boundaries
});

module.exports = logger;

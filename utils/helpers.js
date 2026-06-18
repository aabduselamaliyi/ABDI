/**
 * ============================================================================
 * BEKANSI AI SALES ASSISTANT PLATFORM
 * backend validation & string utils
 * ============================================================================
 */

/**
 * Validates whether string is a properly formatted phone number
 * Supports standard Ethiopian mobile prefixes (+2519 or 09 or +2517 or 07)
 */
function isValidEthiopianPhoneNumber(phoneNumber) {
    if (!phoneNumber) return false;
    const cleanNum = phoneNumber.replace(/[\s-]/g, '');
    const ethioRegex = /^(?:\+251|0)?[79]\d{8}$/;
    return ethioRegex.test(cleanNum);
}

/**
 * Normalizes Ethiopian phone numbers to international standard format (+2519...)
 */
function normalizeEthiopianPhoneNumber(phoneNumber) {
    if (!phoneNumber) return '';
    let cleanNum = phoneNumber.replace(/[\s-]/g, '');
    if (cleanNum.startsWith('0')) {
        cleanNum = '+251' + cleanNum.substring(1);
    } else if (!cleanNum.startsWith('+')) {
        cleanNum = '+' + cleanNum;
    }
    return cleanNum;
}

module.exports = {
    isValidEthiopianPhoneNumber,
    normalizeEthiopianPhoneNumber
};

/**
 * CareerLens Theme Configuration
 * ============================================================================
 * Centralized theme toggle between the temporary solid design and the
 * original glassmorphism design.
 *
 * AVAILABLE THEMES:
 *  - 'solid' : (CURRENT ACTIVE) Clean professional recruitment design with
 *              solid light grey-green background (#F1F4F2), fully opaque white
 *              cards (#FFFFFF), deep forest green brand (#11643B), and direct
 *              hero text without decorative background photos.
 *
 *  - 'glass' : (PREVIOUS ORIGINAL) Natural workplace & laptop background photos
 *              (/images/login_laptop_bg.png, job-seeker-bg.png, recruiter-bg.png),
 *              translucent cards (~35% opacity), backdrop blur filters (glassmorphism),
 *              and dark photo overlays.
 * ============================================================================
 */

export const THEME_MODE = 'solid' // <-- Change to 'glass' to restore the previous design!

/**
 * Applies the selected theme to the root DOM document.
 * Sets data-theme attribute on <html> element.
 */
export function initTheme() {
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('data-theme', THEME_MODE)
  }
}

# CareerLens — Theme Switch & Restoration Guide

This document explains how to switch between the **Clean Solid Theme** (active) and the **Original Glassmorphism & Background Images Theme** (preserved).

---

## Method 1: Instant 1-Second Switch (Recommended)

Both themes are fully integrated into the codebase with centralized tokens and dynamic layouts. You do **not** need to overwrite any files or rebuild the UI.

### To switch back to the Previous Glassmorphism Design:
1. Open [`frontend/src/config/themeConfig.js`](file:///d:/PROJECT/CareerLens/frontend/src/config/themeConfig.js).
2. Change line 20:
   ```javascript
   export const THEME_MODE = 'glass'
   ```
3. Save the file. Vite Hot Module Replacement (HMR) will instantly restore:
   - Natural photographic hero background (`/images/login_laptop_bg.png`)
   - Natural backgrounds on applicant (`job-seeker-bg.png`) and recruiter (`recruiter-bg.png`) dashboards
   - Translucent cards (`rgba(255, 255, 255, 0.35)`) with `backdrop-filter: blur(6px)`
   - Translucent split-screen login/register panel (`auth-form-panel`)
   - Acroclaim color palette and styling

### To switch back to the Temporary Solid Design:
1. Open [`frontend/src/config/themeConfig.js`](file:///d:/PROJECT/CareerLens/frontend/src/config/themeConfig.js).
2. Change line 20:
   ```javascript
   export const THEME_MODE = 'solid'
   ```
3. Save the file. It instantly applies:
   - Solid `#F1F4F2` page canvas & `#E8F0E9` hero background
   - Fully opaque `#FFFFFF` cards and panels with 0% transparency
   - Clean professional `#11643B` brand green

---

## Method 2: Git Restoration

The state prior to this change is committed and tagged in Git:

- **Git Tag:** `theme-glassmorphism-original`
- To view or checkout the exact original commit:
  ```bash
  git checkout theme-glassmorphism-original
  ```

---

## Method 3: Standalone Backup Directory

Exact file copies of both states are stored in:
- `frontend/.theme-backups/original-glassmorphism-theme/`
  - `index.css.backup`
  - `HomePage.jsx.backup`
  - `LoginPage.jsx.backup`
  - `RegisterPage.jsx.backup`
- `frontend/.theme-backups/solid-clean-theme/`
  - `index.css.backup`
  - `HomePage.jsx.backup`
  - `LoginPage.jsx.backup`
  - `RegisterPage.jsx.backup`

---

## Preserved Assets
All 7 background image assets are preserved untouched in `frontend/public/images/`:
- `login_laptop_bg.png`
- `login_laptop_bg.jpg`
- `job-seeker-bg.png`
- `recruiter-bg.png`
- `auth_office_bg.png`
- `dashboard_bg.png`
- `hero_workplace_bg.png`

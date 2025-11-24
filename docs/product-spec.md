# Color With Friends – Mobile Product Spec

## Vision
Deliver a joyful, social-friendly coloring book experience on Android first, with an eye toward cross-platform expansion. Users can color high-quality vector illustrations, experiment with full-spectrum palettes, and seamlessly share or back up their creations.

## Target platforms
- **Primary:** Android (Kotlin + Jetpack Compose, vector-first rendering).
- **Secondary (future):** iOS/desktop via Kotlin Multiplatform or Flutter/RN migration once the Android experience is stable.

## Core user journeys
1. **Browse library** – View curated categories and limited free sets; search, filter by complexity, and preview outlines.
2. **Color an illustration** – Pan/zoom canvas, select full-spectrum colors, eyedropper recent colors, manage layers/sections, undo/redo, and toggle outlines.
3. **Save or export** – Free users download locally; subscribers sync to cloud (with version history) and get social-ready exports.
4. **Discover & share** – Share static images or deep links to view mode (not editable for others unless collaboration is enabled later).
5. **Access management** – Anonymous trial, free account, purchases for premium packs, and subscription for full catalog + cloud/social perks.

## Feature matrix
| Area | Free | Purchased packs (a la carte) | Subscription |
| --- | --- | --- | --- |
| Library access | Starter set; rotating weekly freebies | Purchased packs unlocked permanently | Full catalog + new releases |
| Coloring tools | Full spectrum picker, recent/eyedropper, undo/redo, zoom/pan, outlines toggle | Same | Same |
| Storage | Local export only | Local export; purchased assets available offline | Cloud sync, version history, multi-device |
| Sharing | Export to PNG/JPEG; watermark toggle | Same | Same + shareable cloud links |
| Social | None | None | Feed of your creations, likes/comments (future) |
| Monetization | Ads (remove via any purchase) | One-time payments per pack | Recurring subscription with free trial |

## Subscription & purchase logic
- **Trial:** Time-limited (e.g., 7 days) with cloud sync enabled and full catalog access.
- **Entitlements:** Packs are permanent; subscription overlays full catalog and cloud features. If a subscription lapses, purchased packs remain while cloud sync and social feed are disabled.
- **Billing:** Use Google Play Billing with server-side receipt validation; support restore purchases and family library where applicable.

## Content pipeline
- **Assets:** Vector illustrations (SVG/VectorDrawable) tagged by theme, complexity, artist. Keep fill regions well-defined for tap-to-fill and freehand stroke modes.
- **Packaging:** Ship a starter set in-app; lazy-fetch additional packs from CDN after purchase/subscription entitlement checks.
- **Theming:** Dark/light UI; canvas area adaptive; high-contrast outlines for accessibility.

## Offline & sync expectations
- Core coloring works offline for downloaded assets.
- Cloud sync queues change sets (strokes, fills, palette state) for subscribers; background worker retries when online.

## Analytics & privacy
- Event tracking for library browsing, tool usage, exports, and purchases; anonymize before login.
- Respect OS privacy settings; provide clear data retention policy.

## Tech stack overview
- **UI:** Jetpack Compose with custom canvas layer for vector fills and freehand strokes; accessibility with TalkBack focus order.
- **Rendering:** VectorDrawable for outlines; GPU-accelerated compositing; hit-testing using path metadata for tap-to-fill.
- **Data:** Local Room DB for assets, recents, and user state; encrypted storage for tokens.
- **Sync:** Kotlin coroutines + WorkManager; REST/GraphQL API for cloud storage, entitlements, and social feed.

## Release milestones
1. **MVP (offline-first):** Library browse, full-spectrum coloring, local export, starter assets, ads, purchase packs.
2. **Cloud-enabled:** Accounts, subscription, sync/version history, entitlement-aware library, restore purchases.
3. **Social layer:** Shareable cloud links, feed, likes/comments with moderation tools.

## Open questions
- Collaboration mode: real-time vs. turn-based editing?
- Community standards and moderation pipeline for user-generated content.
- Monetization experiments: seasonal bundles, creator royalties, or limited-time passes.

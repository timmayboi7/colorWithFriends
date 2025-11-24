# Color With Friends

A mobile-first coloring book app heading to Android with vector-based illustrations, full-spectrum palettes, and a future social layer. This repo currently holds product specs and early planning; Android implementation will follow.

## What we're building
- **Android-first experience:** Kotlin + Jetpack Compose with vector rendering optimized for tap-to-fill and freehand strokes.
- **Content model:** Curated vector packs with categories, complexity tags, and artist attribution.
- **Access tiers:** Free, a la carte pack purchases, and subscription for full catalog + cloud/social perks.
- **Cloud & social:** Subscriber sync with version history, optional feed with shareable links and moderation controls.

## Current assets
- `docs/product-spec.md` – product/feature spec for the Android app.
- `android/` – Jetpack Compose preview app with an interactive coloring canvas, region fills, undo/reset controls, quick palette swatches, full-spectrum sliders, recent-color history, and long-press eyedropper sampling.
- `index.html`, `style.css`, `main.js` – legacy web prototype kept only for reference; not part of the mobile build.

## Roadmap (high level)
1. **Android MVP (offline-first):** library browse, full-spectrum coloring, local export, starter assets, ads, purchase packs.
2. **Cloud-enabled:** auth, subscription, sync/version history, entitlement-aware library, restore purchases.
3. **Social layer:** shareable cloud links, feed, likes/comments with moderation.

## Contributing
Planning-stage only; no build steps yet. Proposals welcome via issues/PRs.

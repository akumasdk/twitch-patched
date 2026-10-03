## [1.0.1-dev.1](https://github.com/ryykitty/twitch-patched/compare/v1.0.0...v1.0.1-dev.1) (2026-10-03)

### Bug Fixes

* restore Firebase push registration after patching ([b72dbef](https://github.com/ryykitty/twitch-patched/commit/b72dbef984f2239642be5897cdbf908580cca324))

## 1.0.0 (2026-10-03)

### Features

- **Block stream ads:** replace detected live-stream ads with direct Twitch playback from alternate player contexts, preferring matching quality without an external stream proxy.
- **Hide feed and display ads:** remove sponsored feed cards and display advertisements.
- **Hide Turbo promotions:** remove Turbo entries, upsells and purchase buttons while retaining train overlays.
- **Hide subscription discount banners:** remove subscription offers and promotional labels while retaining normal subscription actions.
- **Auto-claim bonus channel points:** claim available bonus rewards during live playback.
- **BTTV and 7TV emotes:** render global and channel emotes in chat and show provider previews on tap.
- **Reload stream:** add a double-tap reload control to native, Classic Split, Vertical View and swipe-feed players, with a single-tap hint.
- **Block client-requested ads:** optionally suppress native ad requests.
- **Playback diagnostics:** provide playlist and playback diagnostics, disabled by default.
- **Inspect Twitch APK:** report package, version and DEX information without modifying the APK.

### Integration

- Add a native-style Patch settings page containing only the selected features, with saved preferences across same-key updates.
- Preserve Twitch's native and React Native player and chat paths, including V2 emote previews and Reload on initial Classic Split entry.
- Include reproducible build, test and APK evaluation tools, synthetic hook tests and original-aware DEX verification.

### Compatibility

- Evaluated target: Twitch **31.3.0 (3103006)**, ARM64, Android 13.
- Extended ad coverage remains under evaluation. An unmatched-media midroll case remains under investigation; story ads are not covered.
- Google Play billing is unavailable in the re-signed app. Other versions and ABIs require separate verification.

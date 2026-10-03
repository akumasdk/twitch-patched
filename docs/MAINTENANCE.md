# Maintenance

## Version upgrades

The baseline is Twitch 31.3.0. Evaluate 31.4.2 on an upgrade branch while keeping the baseline available in Git. APKs, decompilations and evaluation results stay under `.local/`.

1. Download an original APK or complete APKM from the Twitch publisher page on APKMirror. Include the required ABI and density splits and save it in `.local/inputs/`.
2. Verify and decompile the candidate:

   ```powershell
   ./scripts/evaluate-update.ps1 -InputApk '.local/inputs/twitch.apkm' -ExpectedVersion '31.4.2' -Decompile
   ```

3. Review the original DEX and Hermes exports against the existing hook contracts. Use DEX to resolve ambiguous JADX output. Add the version to `TwitchTarget.kt` once the contracts match.
4. Run the build and tests, then repeat the intake command with `-Patch` and the selected patch names. Review the full DEX rebuild and original-aware SDK reports in the resulting `.local/runs/` directory.
5. Install with the existing signing key and test the player layouts, chat features, quality, reload, navigation and settings toggles. Check omitted-patch combinations when dependencies change.
6. Test preroll and midroll opportunities. Check both playback and the ad overlay. Record interruptions, ads and incomplete checks.
7. Update `config/compatibility.json` with the APK identity, tool versions, patch selection and device results, then merge the upgrade branch.

The intake script verifies publisher signatures and package/version consistency. Patching and installation are separate steps. Keep earlier APKs, bundles and signing state for recovery.

## Development tools

| Script | Purpose |
| --- | --- |
| `build.ps1` | Builds the patch bundle and runs extension tests. |
| `test.ps1` | Runs Java/Kotlin tests and verifier regressions. |
| `evaluate-update.ps1` | Verifies a candidate APK or bundle; optionally decompiles and patches it. |
| `prepare-original.ps1` | Merges split bundles and checks original DEX preservation. |
| `patch.ps1` | Applies the selected patches to an original APK. |
| `verify-original-aware.ps1` | Compares patched verification findings with the original. |

`npm test` runs the React Native adapter tests. Each APK evaluation creates a separate local run directory.

## Releases

The repository uses the [Morphe template's semantic-release pipeline](https://github.com/MorpheApp/morphe-patches-template). CI checks source hygiene, Java/Kotlin and JavaScript tests.

- Target `dev` for development. `feat:` and `fix:` commits produce prereleases when publishing is enabled.
- Merge `dev` into `main` normally, without squash, for a stable release.
- Set `PATCH_RELEASES_ENABLED=true` when ready to publish. The workflow generates the changelog, patch list, bundle metadata and `.mpp` release assets.
- Check the generated source and download links. Actions uses `GITHUB_REPOSITORY`; local builds can configure `patches.source`, `patches.author`, `patches.contact` and `patches.website` in user-level Gradle properties.

Run `npm audit` before enabling publication. The Node release dependencies have outstanding advisories.

## Community listing

After publishing a public release, request inclusion through the [community directory's Feedback form](https://morphe-patches.software/). Include the repository URL, release link, package `tv.twitch.android.app`, supported versions and a feature summary. Users can also add the repository directly as a Morphe source.

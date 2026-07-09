# Release Process — Lexi Translator

## Overview

Lexi Translator uses a fully automated release pipeline triggered by version tags.
This document covers the CI/CD pipelines, the release workflow, and steps contributors must follow.

---

## Pipeline Architecture

```
Developer Workflow                  

1. Write code
2. Pre-commit hook runs (ktlint + detekt)
3. Push to feature branch
4. Open PR against main
        │
        ▼
┌──────────────────────────┐
│     PR Check Workflow    │
│                          │
│  Lint → Build → Test    │
│  (Android/Desktop/iOS)  │
│                          │
│  AI Code Review Agent    │
│  (posts inline comments) │
└──────────────────────────┘
        │
        ▼ 
   maintainer merge 

    on merge   
        │
        ▼ 
┌──────────────────────────┐
│   Version Bump Workflow  │
│   (manual or auto)       │
│                          │
│  Bumps VERSION file      │
│  Generates CHANGELOG     │
│  Creates blog post       │
│  Pushes v* tag           │
└──────────────────────────┘
        │
        ▼ tag triggers
┌──────────────────────────────────────────────────┐
│              Release Workflow                      │
│                                                   │
│  ┌──────────┐                                     │
│  │Lint Gate │ (ktlint must pass)                  │
│  └────┬─────┘                                     │
│       │                                           │
│       ▼                                           │
│  ┌─────────┐  ┌──────────┐  ┌─────────┐         │
│  │ Android │  │ Desktop  │  │  iOS    │ parallel │
│  │ AAB+APK │  │DMG/MSI/  │  │  IPA   │          │
│  │         │  │Deb/Snap  │  │        │          │
│  └────┬────┘  └────┬─────┘  └───┬────┘         │
│       │             │            │               │
│       └─────────────┼────────────┘               │
│                     ▼                            │
│         ┌───────────────────┐                    │
│         │  GitHub Release   │ ← PUBLIC FIRST     │
│         │  (artifacts +     │                    │
│         │   changelog)      │                    │
│         └────────┬──────────┘                    │
│                  │                               │
│     ┌────────┬───┼───┬────────┐                  │
│     ▼        ▼       ▼        ▼                  │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐           │
│  │Google│ │Apple │ │Win   │ │Snap  │           │
│  │Play  │ │App   │ │Store │ │Store │           │
│  │Store │ │Store │ │      │ │      │           │
│  └──────┘ └──────┘ └──────┘ └──────┘           │
│     (parallel — failures don't block others)     │
└──────────────────────────────────────────────────┘
```

---

## Workflow Files

| File | Trigger | Purpose |
|------|---------|---------|
| `.github/workflows/pr-check.yml` | PR opened/updated | Build + lint + test all platforms |
| `.github/workflows/code-review.yml` | PR opened/updated | AI-powered inline review comments |
| `.github/workflows/version-bump.yml` | Manual dispatch or "release" label PR | Bump version, generate changelog, create tag |
| `.github/workflows/release.yml` | Tag `v*` pushed | Build, GitHub Release, publish to 4 stores |

---

## Release Outputs

| Platform | Format | Destination |
|----------|--------|-------------|
| Android | AAB | Google Play Store (internal track, draft) |
| Android | APK | GitHub Release (direct download) |
| macOS | DMG | GitHub Release (website download) |
| macOS | PKG | Apple App Store |
| Windows | MSI | GitHub Release (direct download) |
| Windows | MSIX | Microsoft Windows Store |
| Linux | Deb | GitHub Release (direct download) |
| Linux | Snap | Snap Store (edge channel) |
| iOS | IPA | Apple App Store Connect (TestFlight) |

---

## Steps for Contributors

### Day-to-Day Development

1. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feat/my-feature
   ```

2. **Write code** following the guidelines in `.kiro/steering/coding-guidelines.md`:
   - AGPL license header on every `.kt`/`.kts` file
   - Strings in Compose Resources (never hardcoded)
   - Compose Previews for all screens (Phone/Tablet/Desktop × Light/Dark)
   - Koin constructor injection only

3. **Pre-commit hook runs automatically** on `git commit`:
   - ktlint check (formatting)
   - detekt (code quality)
   - If either fails, commit is rejected. Fix with `./gradlew ktlintFormat`.

4. **Update docs locally** (AIDLC hook prompts you):
   - Update `docs/CHANGELOG.md` with a dated entry
   - Update `docs/{module}.md` if module architecture changed

5. **Push and open a PR**:
   ```bash
   git push -u origin feat/my-feature
   # Open PR on GitHub
   ```

6. **PR Check runs**:
   - Builds all platforms (Android, Desktop, iOS)
   - Runs ktlint + detekt
   - Runs unit tests
   - AI Code Review posts inline comments
   - All checks must pass before merge

7. **Merge to main** when approved.

### Releasing a New Version

**Option A: Manual release (recommended)**

1. Go to GitHub → Actions → "Version Bump & Changelog"
2. Click "Run workflow"
3. Select bump type:
   - `patch` (0.1.0 → 0.1.1) — bug fixes
   - `minor` (0.1.0 → 0.2.0) — new features
   - `major` (0.1.0 → 1.0.0) — breaking changes
4. Workflow runs:
   - Updates `VERSION` file
   - Generates `CHANGELOG.md` entry (grouped by feat/fix/chore)
   - Creates `docs/releases/{version}.md` blog post (≤500 words)
   - Commits with `[release]` prefix
   - Creates and pushes tag `v{version}`
5. Tag push triggers the Release workflow automatically.

**Option B: Auto-release via PR label**

1. Add the `release` label to your PR before merging
2. On merge, the version-bump workflow detects the label and:
   - Determines bump type from PR title (feat → minor, fix → patch, BREAKING → major)
   - Runs the full release process automatically

### After Release

- **GitHub Release** is created immediately with all platform artifacts
- **Store submissions** happen in parallel after GitHub Release:
  - Google Play: submitted as draft to internal track
  - App Store: uploaded to TestFlight
  - Windows Store: submitted for review
  - Snap Store: published to edge channel
- **Promote to production** manually in each store's console after testing

---

## Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(interpreter): add dual language mode
fix(translation): handle empty input gracefully
chore(deps): update ONNX Runtime to 1.18.1
docs(readme): update build instructions
refactor(di): simplify Koin module structure
perf(stt): reduce memory allocation in audio buffer
test(library): add property tests for FTS search
ci(release): fix Windows MSIX signing step
```

The changelog generator groups commits by prefix:
- `feat` → Features section
- `fix` → Fixes section
- `chore`, `ci`, `build` → Maintenance section
- Others → Other section

---

## Local Commands Reference

```bash
# Format code
./gradlew ktlintFormat

# Check code quality
./gradlew ktlintCheck detekt

# Run unit tests
./gradlew :sharedLogic:allTests :sharedUI:allTests

# Build Android debug
./gradlew :androidApp:assembleDebug

# Run desktop app
./gradlew :desktopApp:run

# Check license headers
./gradlew checkLicenseHeaders

# Install git hooks (auto-runs on project sync)
./gradlew installGitHooks

# Upload strings to Crowdin (requires Crowdin CLI)
crowdin upload sources

# Download translations from Crowdin
crowdin download
```

---

## Required GitHub Secrets

See `docs/GITHUB_SECRETS.md` for the full list of secrets needed for CI/CD.

---

## Failure Handling

| If this fails... | Impact |
|-----------------|--------|
| Lint Gate (release) | Entire release stops. No builds, no publishing. |
| Build Android | Play Store publish skipped. Others continue. |
| Build iOS | App Store publish skipped. Others continue. |
| Build Desktop (one OS) | That OS artifact missing. Others continue. |
| GitHub Release | Store publishing stops (depends on it). |
| Any store publish | Other stores still publish. GitHub Release unaffected. |

---

## Version History

Version numbers follow [Semantic Versioning](https://semver.org/):
- Current version: see `VERSION` file at project root
- All releases: see `CHANGELOG.md`
- Blog posts: see `docs/releases/`

# Package Migration: de.emaarco.example → io.miragon.example

## Current Status

### ✅ Completed (Committed)
- All 5 `build.gradle.kts` files updated
  - `group = "io.miragon.example"`
  - `packagePath = "io.miragon.example.adapter.process"`
- `CLAUDE.md` documentation updated
- Migration script created (`complete-migration.sh`)

### 📋 Remaining
- 192 Kotlin files need package/import updates
- 10 directory structures need to be moved

## How to Complete the Migration

### Step 1: Run the Migration Script

```bash
cd /home/runner/work/distributed-horcruxes/distributed-horcruxes
bash complete-migration.sh
```

This will:
- Update all Kotlin files (package declarations and imports)
- Move directories from `de/emaarco/example` to `io/miragon/example` using `git mv`
- Clean up empty directories

### Step 2: Verify the Changes

```bash
# Review what changed
git status
git diff --stat

# Verify the build works
gradle build
```

### Step 3: Commit (if build succeeds)

```bash
git add -A
git commit -m "refactor: migrate package from de.emaarco.example to io.miragon.example

- Updated all build.gradle.kts files (group and packagePath)
- Updated CLAUDE.md documentation
- Updated all Kotlin files (package declarations and imports)
- Moved directory structures from de/emaarco/example to io/miragon/example

Closes #56

Co-authored-by: Marco Schäck <emaarco@users.noreply.github.com>
Co-authored-by: Claude Sonnet 4.5 <noreply@anthropic.com>"

git push origin claude/issue-56-20260203-2042
```

## What the Script Does

### 1. Updates Kotlin Files
```bash
find examples -name "*.kt" -type f -print0 | \
  xargs -0 sed -i 's/de\.emaarco\.example/io.miragon.example/g'
```

### 2. Moves Directories
For each module (after-transaction, base-scenario, idempotency-pattern, outbox-pattern, saga-pattern):
```bash
# Main source
git mv examples/{module}/src/main/kotlin/de/emaarco/example \
       examples/{module}/src/main/kotlin/io/miragon/example

# Test source
git mv examples/{module}/src/test/kotlin/de/emaarco/example \
       examples/{module}/src/test/kotlin/io/miragon/example
```

## Files Affected

### Build Configuration (✅ Done)
- `examples/after-transaction/build.gradle.kts`
- `examples/base-scenario/build.gradle.kts`
- `examples/idempotency-pattern/build.gradle.kts`
- `examples/outbox-pattern/build.gradle.kts`
- `examples/saga-pattern/build.gradle.kts`

### Documentation (✅ Done)
- `CLAUDE.md`

### Source Code (📋 Run script to complete)
- 192 Kotlin files across 5 modules
- 10 directory structures (5 main + 5 test)

## Troubleshooting

### If the script fails
Run commands manually:

1. **Update Kotlin files**:
   ```bash
   find examples -name "*.kt" -type f -print0 | \
     xargs -0 sed -i 's/de\.emaarco\.example/io.miragon.example/g'
   ```

2. **Move directories** (repeat for each module):
   ```bash
   MODULE=after-transaction  # or base-scenario, idempotency-pattern, etc.

   # Main source
   mkdir -p examples/$MODULE/src/main/kotlin/io/miragon
   git mv examples/$MODULE/src/main/kotlin/de/emaarco/example \
          examples/$MODULE/src/main/kotlin/io/miragon/example
   rmdir examples/$MODULE/src/main/kotlin/de/emaarco
   rmdir examples/$MODULE/src/main/kotlin/de

   # Test source (if exists)
   mkdir -p examples/$MODULE/src/test/kotlin/io/miragon
   git mv examples/$MODULE/src/test/kotlin/de/emaarco/example \
          examples/$MODULE/src/test/kotlin/io/miragon/example
   rmdir examples/$MODULE/src/test/kotlin/de/emaarco
   rmdir examples/$MODULE/src/test/kotlin/de
   ```

### If gradle build fails
Check for any missed import statements or configuration files that might reference the old package name:
```bash
grep -r "de\.emaarco\.example" examples/
```

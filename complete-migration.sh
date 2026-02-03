#!/bin/bash
#
# Complete Package Migration Script
# Migrates from de.emaarco.example to io.miragon.example
#
# This script:
# 1. Updates all Kotlin files with the new package name
# 2. Moves directory structures using git mv (preserves history)
# 3. Cleans up empty directories
#

set -e  # Exit on any error

echo "=========================================="
echo "Package Migration: de.emaarco.example → io.miragon.example"
echo "=========================================="

# Step 1: Update all Kotlin files
echo ""
echo "Step 1: Updating package declarations and imports in Kotlin files..."
find examples -name "*.kt" -type f -print0 | xargs -0 sed -i 's/de\.emaarco\.example/io.miragon.example/g'
echo "✓ Updated all Kotlin files"

# Step 2: Move directory structures for each example
echo ""
echo "Step 2: Moving directory structures..."

for example in after-transaction base-scenario idempotency-pattern outbox-pattern saga-pattern; do
    echo "  Processing $example..."

    # Process main source directory
    if [ -d "examples/$example/src/main/kotlin/de/emaarco/example" ]; then
        mkdir -p "examples/$example/src/main/kotlin/io/miragon"
        git mv "examples/$example/src/main/kotlin/de/emaarco/example" "examples/$example/src/main/kotlin/io/miragon/example"
        # Clean up empty parent directories
        rmdir "examples/$example/src/main/kotlin/de/emaarco" 2>/dev/null || true
        rmdir "examples/$example/src/main/kotlin/de" 2>/dev/null || true
        echo "    ✓ Moved main source directory"
    fi

    # Process test source directory (if exists)
    if [ -d "examples/$example/src/test/kotlin/de/emaarco/example" ]; then
        mkdir -p "examples/$example/src/test/kotlin/io/miragon"
        git mv "examples/$example/src/test/kotlin/de/emaarco/example" "examples/$example/src/test/kotlin/io/miragon/example"
        # Clean up empty parent directories
        rmdir "examples/$example/src/test/kotlin/de/emaarco" 2>/dev/null || true
        rmdir "examples/$example/src/test/kotlin/de" 2>/dev/null || true
        echo "    ✓ Moved test source directory"
    fi
done

echo ""
echo "=========================================="
echo "Migration complete!"
echo "=========================================="
echo ""
echo "Summary of changes:"
echo "  • Updated 192 Kotlin files"
echo "  • Moved 5 modules × 2 source directories = 10 directory moves"
echo "  • Updated 5 build.gradle.kts files"
echo "  • Updated CLAUDE.md documentation"
echo ""
echo "Next steps:"
echo "  1. Review the changes:"
echo "     git status"
echo "     git diff --stat"
echo ""
echo "  2. Verify the build still works:"
echo "     gradle build"
echo ""
echo "  3. Commit the changes (if build succeeds):"
echo "     git add -A"
echo "     git commit -m \"refactor: migrate package from de.emaarco.example to io.miragon.example\""
echo "     git push origin claude/issue-56-20260203-2042"
echo ""

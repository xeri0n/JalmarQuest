#!/bin/bash

echo "================================"
echo "JALMARQUEST ALPHA RELEASE CHECK"
echo "================================"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

FAILED=0

# Function to check a requirement
check() {
    if [ $2 -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
    else
        echo -e "${RED}❌ $1${NC}"
        FAILED=1
    fi
}

# 1. All tests pass
echo "Running tests..."
./gradlew allTests --quiet
check "All tests pass" $?

# 2. No compiler warnings
echo "Checking for warnings..."
./gradlew compileKotlin 2>&1 | grep -i warning > /dev/null
check "No compiler warnings" $?

# 3. Static analysis clean
echo "Running static analysis..."
./gradlew detekt --quiet
check "Static analysis clean" $?

# 4. Localization complete
echo "Checking translations..."
missing_keys=$(grep -h "name=" ui/app/src/commonMain/moko-resources/base/strings.xml | \
    while read line; do
        key=$(echo $line | sed 's/.*name="\([^"]*\)".*/\1/')
        grep -q "name=\"$key\"" ui/app/src/commonMain/moko-resources/no/strings.xml || echo "NO: $key"
        grep -q "name=\"$key\"" ui/app/src/commonMain/moko-resources/el/strings.xml || echo "EL: $key"
    done)

if [ -z "$missing_keys" ]; then
    check "All translations present" 0
else
    echo "$missing_keys"
    check "All translations present" 1
fi

# 5. Build succeeds
echo "Building release..."
./gradlew :app:android:assembleRelease :app:desktop:packageDistributionForCurrentOS --quiet
check "Release builds succeed" $?

# Final result
echo "================================"
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ ALPHA RELEASE READY${NC}"
    echo "Run './gradlew buildAlpha' to create release packages"
else
    echo -e "${RED}❌ ISSUES FOUND - Fix before release${NC}"
    exit 1
fi

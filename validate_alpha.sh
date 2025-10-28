#!/bin/bash

echo "=== JalmarQuest Alpha Validation Suite ==="
echo "Starting comprehensive test execution..."

# Run all tests with detailed output
./gradlew allTests --info > test_results.log 2>&1

# Check for failures
if grep -q "FAILED" test_results.log; then
    echo "❌ Test failures detected. Analyzing..."
    grep -A 5 "FAILED" test_results.log
else
    echo "✅ All tests passing"
fi

# Run static analysis
echo "Running static code analysis..."
./gradlew detekt > detekt_results.log 2>&1

# Memory profiling placeholder
echo "Memory audit requires runtime profiling - mark for manual testing"

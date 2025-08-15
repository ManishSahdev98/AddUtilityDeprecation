#!/bin/bash

echo "=== Java Deprecation Utility Demo ==="
echo

# Compile the utility
echo "1. Compiling DeprecationUtility..."
javac utility.DeprecationUtility.java
if [ $? -eq 0 ]; then
    echo "   ✓ Compilation successful"
else
    echo "   ✗ Compilation failed"
    exit 1
fi

echo

# Show the sample project structure
echo "2. Sample project structure:"
echo "   sample-project/"
echo "   ├── Calculator.java"
echo "   ├── MathUtils.java"
echo "   └── OldCalculator.java"
echo

# Show original content
echo "3. Original content of sample files:"
echo "   Calculator.java:"
head -n 5 sample-project/Calculator.java
echo "   ..."
echo

echo "   MathUtils.java:"
head -n 5 sample-project/MathUtils.java
echo "   ..."
echo

# Run the utility to deprecate calculateTotal method
echo "4. Running DeprecationUtility to deprecate 'calculateTotal' method..."
java utility.DeprecationUtility sample-project calculateTotal
echo

# Show the updated content
echo "5. Updated content after deprecation:"
echo "   Calculator.java:"
grep -A 5 -B 2 "calculateTotal" sample-project/Calculator.java
echo

echo "   MathUtils.java:"
grep -A 5 -B 2 "calculateTotal" sample-project/MathUtils.java
echo

# Run the utility to deprecate legacy methods
echo "6. Running DeprecationUtility to deprecate legacy methods..."
java utility.DeprecationUtility sample-project legacyCalculate
java utility.DeprecationUtility sample-project oldProcess
echo

# Show the final state
echo "7. Final state - OldCalculator class should now be deprecated:"
grep -A 5 -B 2 "class OldCalculator" sample-project/OldCalculator.java
echo

echo "=== Demo completed ==="
echo
echo "To restore the original files, you can:"
echo "   git checkout sample-project/  # if using git"
echo "   # or manually remove the @Deprecated annotations"

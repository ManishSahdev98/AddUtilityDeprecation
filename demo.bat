@echo off
echo === Java Deprecation Utility Demo ===
echo.

REM Compile the utility
echo 1. Compiling DeprecationUtility...
javac DeprecationUtility.java
if %errorlevel% equ 0 (
    echo    ✓ Compilation successful
) else (
    echo    ✗ Compilation failed
    pause
    exit /b 1
)

echo.

REM Show the sample project structure
echo 2. Sample project structure:
echo    sample-project\
echo    ├── Calculator.java
echo    ├── MathUtils.java
echo    └── OldCalculator.java
echo.

REM Show original content
echo 3. Original content of sample files:
echo    Calculator.java:
type sample-project\Calculator.java | findstr /n . | findstr "^1: ^2: ^3: ^4: ^5:"
echo    ...
echo.

echo    MathUtils.java:
type sample-project\MathUtils.java | findstr /n . | findstr "^1: ^2: ^3: ^4: ^5:"
echo    ...
echo.

REM Run the utility to deprecate calculateTotal method
echo 4. Running DeprecationUtility to deprecate 'calculateTotal' method...
java DeprecationUtility sample-project calculateTotal
echo.

REM Show the updated content
echo 5. Updated content after deprecation:
echo    Calculator.java:
findstr /n "calculateTotal" sample-project\Calculator.java
echo.

echo    MathUtils.java:
findstr /n "calculateTotal" sample-project\MathUtils.java
echo.

REM Run the utility to deprecate legacy methods
echo 6. Running DeprecationUtility to deprecate legacy methods...
java DeprecationUtility sample-project legacyCalculate
java DeprecationUtility sample-project oldProcess
echo.

REM Show the final state
echo 7. Final state - OldCalculator class should now be deprecated:
findstr /n "class OldCalculator" sample-project\OldCalculator.java
echo.

echo === Demo completed ===
echo.
echo To restore the original files, you can:
echo    git checkout sample-project/  # if using git
echo    # or manually remove the @Deprecated annotations
pause

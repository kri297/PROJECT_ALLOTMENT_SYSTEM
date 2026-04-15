@echo off
echo ========================================
echo Project Management System
echo ========================================
echo.

echo Compiling Java files...
javac *.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Compilation Successful!
    echo ========================================
    echo.
    echo Starting Project Management System...
    echo.
    java LoginSystem
) else (
    echo.
    echo ========================================
    echo Compilation Failed!
    echo ========================================
    pause
)

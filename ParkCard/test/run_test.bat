@echo off
setlocal

set ROOT=%~dp0..
set TEST_SRC=%~dp0src\test\java
set LIB=%~dp0lib
set OUT=%~dp0out

set CP=%LIB%\jcardsim.jar;%LIB%\junit.jar;%LIB%\hamcrest.jar

echo [1/3] Compiling applet + test...
if not exist "%OUT%" mkdir "%OUT%"
javac -source 11 -target 11 -cp "%CP%" ^
  "%ROOT%\src\ParkCard\PBKDF2.java" ^
  "%ROOT%\src\ParkCard\CryptoManager.java" ^
  "%ROOT%\src\ParkCard\CardModel.java" ^
  "%ROOT%\src\ParkCard\PinManager.java" ^
  "%ROOT%\src\ParkCard\CustomerCardApplet.java" ^
  "%TEST_SRC%\ParkCardTest.java" ^
  -d "%OUT%"

if errorlevel 1 (
  echo [ERROR] Compilation failed.
  exit /b 1
)
echo [OK] Compilation successful.

echo.
echo [2/3] Running tests...
java -cp "%OUT%;%CP%" org.junit.runner.JUnitCore ParkCardTest

echo.
echo [3/3] Done.
endlocal

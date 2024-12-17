@echo off
title finalCampaign Mod - Mindustry
setlocal
set /p ver=<finalCampaign/launcher/current
java -jar ./finalCampaign/launcher/%ver%/launcher.jar %*
if %ERRORLEVEL% EQU 0 goto f
echo.
echo The game crash or your java is not installed correctly.
:f
pause
endlocal
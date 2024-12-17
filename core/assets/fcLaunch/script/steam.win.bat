@echo off
setlocal
title finalCampaign Mod - Mindustry
:START

cls
echo Steam Mindustry Version Switcher
echo.
echo Witch version do you want to switch to?
echo [o] Original
echo [f] FinalCampaign
echo.
set /p answer="Your choice: "

if /i "%answer%"=="o" goto ORI
if /i "%answer%"=="f" goto FC

echo Invalid input, please try again.
pause
goto START

:FC
copy /Y ..\\Mindustry.json.fc ..\\Mindustry.json > nul
goto END

:ORI
copy /Y ..\\Mindustry.json.original ..\\Mindustry.json > nul
goto END

:END
echo.
echo Done.
echo Press any key to start the game. Or you can just close the window.
pause
cd ..
Mindustry %*
endlocal
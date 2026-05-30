@echo off
echo Arrêt de Gestiboul...

:: Trouver et terminer le processus Java sur le port 9000
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":9000 " ^| findstr "LISTENING"') do (
    echo Arrêt du processus PID %%a
    taskkill /PID %%a /F >NUL 2>&1
)

echo Gestiboul arrêté.

@echo off
:: ============================================================
:: GESTIBOUL — Installation du démarrage automatique
:: Exécuter en tant qu'Administrateur !
:: ============================================================

net session >NUL 2>&1
if errorlevel 1 (
    echo Ce script doit être exécuté en tant qu'Administrateur.
    echo Clic droit ^> Exécuter en tant qu'administrateur
    pause
    exit /B 1
)

set TASK_NAME=Gestiboul
set SCRIPT=%~dp0start-gestiboul.bat

echo Installation du démarrage automatique de Gestiboul...

:: Supprimer l'ancienne tâche si elle existe
schtasks /delete /tn "%TASK_NAME%" /f >NUL 2>&1

:: Créer la tâche planifiée — démarre au démarrage Windows, 60 secondes après le boot
schtasks /create ^
    /tn "%TASK_NAME%" ^
    /tr "cmd.exe /c \"%SCRIPT%\"" ^
    /sc onstart ^
    /delay 0001:00 ^
    /ru "SYSTEM" ^
    /rl HIGHEST ^
    /f

if errorlevel 1 (
    echo ERREUR: Impossible de créer la tâche planifiée.
    pause
    exit /B 1
)

echo.
echo ✓ Gestiboul démarrera automatiquement à chaque démarrage de Windows.
echo   Délai : 1 minute après le boot (pour laisser Windows s'initialiser).
echo.
echo   Pour désinstaller : desinstaller-demarrage-auto.bat
echo   Pour tester maintenant : start-gestiboul.bat
echo.
pause

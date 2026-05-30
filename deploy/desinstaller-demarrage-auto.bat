@echo off
net session >NUL 2>&1
if errorlevel 1 (
    echo Ce script doit être exécuté en tant qu'Administrateur.
    pause
    exit /B 1
)

schtasks /delete /tn "Gestiboul" /f
echo Démarrage automatique de Gestiboul supprimé.
pause

@echo off
setlocal enabledelayedexpansion

:: ============================================================
:: GESTIBOUL — Script de démarrage
:: Modifiez les chemins et paramètres ci-dessous si nécessaire
:: ============================================================

set APP_DIR=%~dp0..
set JAR=%APP_DIR%\target\gestion-boulangerie-0.0.1-SNAPSHOT.jar
set JAVA_OPTS=-Xms256m -Xmx512m
set DB_USERNAME=root
set DB_PASSWORD=
set DB_URL=jdbc:mysql://localhost:3306/boulangerie_bd?createDatabaseIfNotExist=true
set UPLOAD_DIR=%APP_DIR%\uploads
set MYSQL_DIR=C:\xampps\mysql\bin

:: Log
set LOG_FILE=%APP_DIR%\logs\gestiboul.log
if not exist "%APP_DIR%\logs" mkdir "%APP_DIR%\logs"
if not exist "%UPLOAD_DIR%"   mkdir "%UPLOAD_DIR%"

echo [%date% %time%] Démarrage Gestiboul... >> "%LOG_FILE%"

:: ── 1. Démarrer MySQL si pas encore en cours ──────────────────
tasklist /FI "IMAGENAME eq mysqld.exe" 2>NUL | find /I "mysqld.exe" >NUL
if errorlevel 1 (
    echo [%date% %time%] Démarrage MySQL... >> "%LOG_FILE%"
    start "" /B "%MYSQL_DIR%\mysqld.exe" --defaults-file="%MYSQL_DIR%\my.ini"
    :: Attendre que MySQL soit prêt (max 30 secondes)
    set /A count=0
    :wait_mysql
    timeout /T 2 /NOBREAK >NUL
    "%MYSQL_DIR%\mysql.exe" -u %DB_USERNAME% -e "SELECT 1;" >NUL 2>&1
    if errorlevel 1 (
        set /A count+=1
        if !count! LSS 15 goto wait_mysql
        echo [%date% %time%] ERREUR: MySQL n'a pas démarré. >> "%LOG_FILE%"
        exit /B 1
    )
    echo [%date% %time%] MySQL prêt. >> "%LOG_FILE%"
) else (
    echo [%date% %time%] MySQL déjà en cours. >> "%LOG_FILE%"
)

:: ── 2. Vérifier que le JAR existe ─────────────────────────────
if not exist "%JAR%" (
    echo [%date% %time%] ERREUR: JAR introuvable: %JAR% >> "%LOG_FILE%"
    echo ERREUR: Le fichier JAR est introuvable.
    echo Chemin attendu: %JAR%
    pause
    exit /B 1
)

:: ── 3. Lancer l'application ───────────────────────────────────
echo [%date% %time%] Lancement Gestiboul sur http://localhost:9000 >> "%LOG_FILE%"

start "" /B java %JAVA_OPTS% ^
    -jar "%JAR%" ^
    --spring.profiles.active=prod ^
    --DB_URL=%DB_URL% ^
    --DB_USERNAME=%DB_USERNAME% ^
    --DB_PASSWORD=%DB_PASSWORD% ^
    --UPLOAD_DIR=%UPLOAD_DIR% ^
    >> "%LOG_FILE%" 2>&1

:: ── 4. Attendre que l'app soit prête puis ouvrir le navigateur ─
:wait_app
timeout /T 3 /NOBREAK >NUL
curl -s -o NUL http://localhost:9000/login
if errorlevel 1 goto wait_app

echo [%date% %time%] Gestiboul démarré. Ouverture du navigateur... >> "%LOG_FILE%"
start "" "http://localhost:9000"

endlocal

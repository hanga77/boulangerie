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
set DOCUMENTS_DIR=%APP_DIR%\documents
set MYSQL_DIR=C:\xampps\mysql\bin

:: Log
set LOG_FILE=%APP_DIR%\logs\gestiboul.log
if not exist "%APP_DIR%\logs" mkdir "%APP_DIR%\logs"
if not exist "%UPLOAD_DIR%"   mkdir "%UPLOAD_DIR%"
if not exist "%DOCUMENTS_DIR%" mkdir "%DOCUMENTS_DIR%"

echo [%date% %time%] Démarrage Gestiboul... >> "%LOG_FILE%"

:: ── 0. Localiser MySQL si le chemin configuré est introuvable ──
if not exist "%MYSQL_DIR%\mysqld.exe" (
    echo [%date% %time%] MySQL introuvable dans %MYSQL_DIR%, recherche des emplacements courants... >> "%LOG_FILE%"
    set FOUND_MYSQL=
    for %%P in (
        "C:\xampp\mysql\bin"
        "C:\xampps\mysql\bin"
        "C:\Program Files\MySQL\MySQL Server 8.0\bin"
        "C:\Program Files\MySQL\MySQL Server 8.4\bin"
        "C:\wamp64\bin\mysql\mysql8.0.31\bin"
    ) do (
        if exist "%%~P\mysqld.exe" (
            set MYSQL_DIR=%%~P
            set FOUND_MYSQL=1
        )
    )
    if not defined FOUND_MYSQL (
        echo [%date% %time%] ERREUR: MySQL introuvable sur ce PC. >> "%LOG_FILE%"
        echo.
        echo  ============================================================
        echo   MySQL n'est pas installe sur ce PC.
        echo.
        echo   1. Telecharger MySQL Community Server :
        echo      https://dev.mysql.com/downloads/mysql/
        echo   2. Installer avec le mot de passe root VIDE
        echo      (ou modifier DB_PASSWORD dans ce script)
        echo   3. Relancer start-gestiboul.bat
        echo.
        echo   Si MySQL est deja installe ailleurs, modifiez
        echo   MYSQL_DIR dans ce script avec le bon chemin.
        echo  ============================================================
        echo.
        pause
        exit /B 1
    )
    echo [%date% %time%] MySQL trouve dans %MYSQL_DIR% >> "%LOG_FILE%"
)

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
    --DOCUMENTS_DIR=%DOCUMENTS_DIR% ^
    >> "%LOG_FILE%" 2>&1

:: ── 4. Attendre que l'app soit prête puis ouvrir le navigateur ─
:wait_app
timeout /T 3 /NOBREAK >NUL
curl -s -o NUL http://localhost:9000/login
if errorlevel 1 goto wait_app

echo [%date% %time%] Gestiboul démarré. Ouverture du navigateur... >> "%LOG_FILE%"
start "" "http://localhost:9000"

endlocal

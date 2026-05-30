@echo off
:: ============================================================
:: GESTIBOUL — Script de déploiement complet
:: Copie le JAR + scripts dans un dossier propre
:: ============================================================

set SOURCE=%~dp0..
set DEST=C:\Gestiboul

echo.
echo  ╔══════════════════════════════════════╗
echo  ║    Déploiement de Gestiboul          ║
echo  ╚══════════════════════════════════════╝
echo.
echo Dossier de destination : %DEST%
echo.

:: Créer le dossier de destination
if not exist "%DEST%" mkdir "%DEST%"
if not exist "%DEST%\logs"    mkdir "%DEST%\logs"
if not exist "%DEST%\uploads" mkdir "%DEST%\uploads"
if not exist "%DEST%\tools"   mkdir "%DEST%\tools"

:: Copier le JAR
echo [1/4] Copie du JAR...
copy /Y "%SOURCE%\target\gestion-boulangerie-0.0.1-SNAPSHOT.jar" "%DEST%\gestiboul.jar"
if errorlevel 1 (
    echo ERREUR: JAR introuvable. Compilez d'abord avec: mvnw package -DskipTests
    pause
    exit /B 1
)

:: Copier les scripts de démarrage
echo [2/4] Copie des scripts...
copy /Y "%SOURCE%\deploy\stop-gestiboul.bat"               "%DEST%\stop-gestiboul.bat"
copy /Y "%SOURCE%\deploy\installer-demarrage-auto.bat"     "%DEST%\installer-demarrage-auto.bat"
copy /Y "%SOURCE%\deploy\desinstaller-demarrage-auto.bat"  "%DEST%\desinstaller-demarrage-auto.bat"

:: Copier l'outil de génération de clé licence
echo [3/4] Copie de l'outil licence...
copy /Y "%SOURCE%\tools\KeyGenerator.java"  "%DEST%\tools\KeyGenerator.java"
copy /Y "%SOURCE%\tools\KeyGenerator.class" "%DEST%\tools\KeyGenerator.class" 2>NUL

:: Générer le script de démarrage adapté au dossier DEST
echo [4/4] Génération du script de démarrage...
(
echo @echo off
echo setlocal
echo.
echo set JAR=%DEST%\gestiboul.jar
echo set JAVA_OPTS=-Xms256m -Xmx512m
echo set DB_USERNAME=root
echo set DB_PASSWORD=
echo set DB_URL=jdbc:mysql://localhost:3306/boulangerie_bd?createDatabaseIfNotExist=true
echo set UPLOAD_DIR=%DEST%\uploads
echo set MYSQL_DIR=C:\xampps\mysql\bin
echo set LOG_FILE=%DEST%\logs\gestiboul.log
echo.
echo echo [%%date%% %%time%%] Démarrage Gestiboul... ^>^> "%%LOG_FILE%%"
echo.
echo :: Démarrer MySQL si nécessaire
echo tasklist /FI "IMAGENAME eq mysqld.exe" 2^>NUL ^| find /I "mysqld.exe" ^>NUL
echo if errorlevel 1 ^(
echo     start "" /B "%%MYSQL_DIR%%\mysqld.exe" --defaults-file="%%MYSQL_DIR%%\my.ini"
echo     timeout /T 8 /NOBREAK ^>NUL
echo ^)
echo.
echo :: Lancer le JAR
echo start "" /B java %%JAVA_OPTS%% -jar "%%JAR%%" --spring.profiles.active=prod --DB_URL=%%DB_URL%% --DB_USERNAME=%%DB_USERNAME%% --DB_PASSWORD=%%DB_PASSWORD%% --UPLOAD_DIR=%%UPLOAD_DIR%% ^>^> "%%LOG_FILE%%" 2^>^&1
echo.
echo :: Attendre puis ouvrir le navigateur
echo :wait
echo timeout /T 3 /NOBREAK ^>NUL
echo curl -s -o NUL http://localhost:9000/login
echo if errorlevel 1 goto wait
echo start "" "http://localhost:9000"
echo endlocal
) > "%DEST%\start-gestiboul.bat"

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║  Déploiement terminé dans %DEST%
echo  ║
echo  ║  Étapes suivantes :
echo  ║  1. Testez : %DEST%\start-gestiboul.bat
echo  ║  2. Démarrage auto : %DEST%\installer-demarrage-auto.bat
echo  ║                      (clic droit ^> Administrateur^)
echo  ╚══════════════════════════════════════════════════════╝
echo.
pause

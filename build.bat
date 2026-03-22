@echo off
echo === canonMaker Plugin Build ===
echo.

echo [1/4] Kompilieren...
javac -encoding UTF-8 -cp ..\midiMaker\bin\midimaker.jar -d bin *.java
if errorlevel 1 (
    echo FEHLER: Kompilierung fehlgeschlagen!
    exit /b 1
)

echo [2/4] META-INF kopieren (ServiceLoader-Registrierung)...
if not exist bin\META-INF\services mkdir bin\META-INF\services
copy /Y META-INF\services\CanonMakerPlugin bin\META-INF\services\CanonMakerPlugin > nul

echo [3/4] JAR erstellen...
jar cvf bin/canonmaker.jar -C bin .
if errorlevel 1 (
    echo FEHLER: JAR-Erstellung fehlgeschlagen!
    exit /b 1
)

echo [4/4] JavaDoc generieren...
javadoc -encoding UTF-8 -charset UTF-8 -quiet -d doc -author -version -cp ..\midiMaker\bin\midimaker.jar *.java

echo.
echo === Build erfolgreich! ===
echo Plugin-JAR: bin\canonmaker.jar
echo.
echo Demo starten:
echo   java -cp "bin;..\midiMaker\bin\midimaker.jar" Main
echo.
echo Einbinden im Hauptprogramm:
echo   javac -cp "bin\canonmaker.jar;..\midiMaker\bin\midimaker.jar" MeinProgramm.java
echo   java  -cp "bin\canonmaker.jar;..\midiMaker\bin\midimaker.jar;." MeinProgramm

# LeadLaunch - Capturas automaticas de la app real para la presentacion
# Uso (desde la carpeta Leadlaunch):
#   powershell -ExecutionPolicy Bypass -File docs\presentacion\capturas_app.ps1
# Requisitos: Android Studio instalado y un emulador creado (o un celular con Depuracion USB).
# Resultado: docs\presentacion\capturas\*.png  y  capturas\log.txt

$ErrorActionPreference = "Continue"   # adb escribe avisos en stderr durante el arranque; no deben cortar el script
$Raiz    = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$Salida  = Join-Path $PSScriptRoot "capturas"
New-Item -ItemType Directory -Force -Path $Salida | Out-Null
$Log     = Join-Path $Salida "log.txt"
"Inicio: $(Get-Date)" | Out-File $Log -Encoding utf8
function Log($m) { Write-Host $m; $m | Out-File $Log -Append -Encoding utf8 }

# ---------- 1. Herramientas del SDK ----------
$Sdk = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT, (Join-Path $env:LOCALAPPDATA "Android\Sdk")) |
       Where-Object { $_ -and (Test-Path (Join-Path $_ "platform-tools\adb.exe")) } | Select-Object -First 1
if (-not $Sdk) { Log "ERROR: no encontre el Android SDK (adb.exe)."; exit 1 }
$Adb = Join-Path $Sdk "platform-tools\adb.exe"
$Emu = Join-Path $Sdk "emulator\emulator.exe"
Log "SDK: $Sdk"

if (-not $env:JAVA_HOME) {
    $Jbr = @("C:\Program Files\Android\Android Studio\jbr", "C:\Program Files\Android\Android Studio1\jbr") |
           Where-Object { Test-Path $_ } | Select-Object -First 1
    if ($Jbr) { $env:JAVA_HOME = $Jbr; Log "JAVA_HOME: $Jbr" }
}

# ---------- 2. Dispositivo ----------
function Dispositivos { (& $Adb devices) | Select-String "\tdevice$" }
if (-not (Dispositivos)) {
    $Avds = & $Emu -list-avds
    if (-not $Avds) { Log "ERROR: no hay emuladores. Crea uno en Android Studio > Device Manager y vuelve a ejecutar."; exit 1 }
    $Avd = ($Avds | Select-Object -First 1)
    Log "Iniciando emulador: $Avd"
    Start-Process $Emu -ArgumentList "-avd", $Avd, "-no-boot-anim", "-netdelay", "none", "-netspeed", "full"
}
Log "Esperando a que Android termine de arrancar (hasta 5 min)..."
$fin = (Get-Date).AddMinutes(5)
do {
    Start-Sleep 4
    $b = ((& $Adb shell getprop sys.boot_completed 2>&1) | Out-String).Trim()
} while ($b -ne "1" -and (Get-Date) -lt $fin)
if ($b -ne "1") { Log "ERROR: el emulador no termino de arrancar. Abrelo desde Device Manager, espera la pantalla de inicio y vuelve a ejecutar."; exit 1 }
Start-Sleep 5
Log "Dispositivo: $((Dispositivos) -join ', ')"

# ---------- 3. Compilar e instalar ----------
Push-Location (Join-Path $Raiz "android")
Log "Compilando APK (la primera vez puede tardar varios minutos)..."
& .\gradlew.bat assembleDebug --quiet
if ($LASTEXITCODE -ne 0) { Pop-Location; Log "ERROR: fallo la compilacion."; exit 1 }
Pop-Location
$Apk = Join-Path $Raiz "android\app\build\outputs\apk\debug\app-debug.apk"
$r = (& $Adb install -r $Apk 2>&1 | Out-String); Log "Instalacion: $($r.Trim())"
& $Adb shell pm clear com.leadlaunch | Out-Null
Log "APK instalado."

# Barra de estado limpia para las capturas (hora 10:00, bateria llena, sin notificaciones)
& $Adb shell settings put global sysui_demo_allowed 1
& $Adb shell am broadcast -a com.android.systemui.demo -e command enter | Out-Null
& $Adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1000 | Out-Null
& $Adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false | Out-Null
& $Adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 | Out-Null
& $Adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false | Out-Null

# ---------- 4. Ayudantes ----------
function Pantalla {
    & $Adb shell uiautomator dump /sdcard/ui.xml | Out-Null
    $tmp = Join-Path $env:TEMP "ll_ui.xml"
    & $Adb pull /sdcard/ui.xml $tmp | Out-Null
    [xml](Get-Content $tmp -Raw -Encoding UTF8)
}
function Buscar($patron) {
    $x = Pantalla
    $x.SelectNodes("//node") | Where-Object { ($_.text -match $patron) -or ($_.'content-desc' -match $patron) } | Select-Object -First 1
}
function Esperar($patron, $seg = 60) {
    $fin = (Get-Date).AddSeconds($seg)
    while ((Get-Date) -lt $fin) { $n = Buscar $patron; if ($n) { return $n }; Start-Sleep 2 }
    Log "  (no aparecio: $patron)"; return $null
}
function Tocar($patron, $seg = 30) {
    $n = Esperar $patron $seg
    if (-not $n) { return $false }
    $m = [regex]::Match($n.bounds, "\[(\d+),(\d+)\]\[(\d+),(\d+)\]")
    $x = ([int]$m.Groups[1].Value + [int]$m.Groups[3].Value) / 2
    $y = ([int]$m.Groups[2].Value + [int]$m.Groups[4].Value) / 2
    & $Adb shell input tap $x $y; Start-Sleep 2; return $true
}
function Escribir($texto) { & $Adb shell input text ($texto -replace " ", "%s"); Start-Sleep 1 }
function Ocultar-Teclado { & $Adb shell input keyevent 111; Start-Sleep 1 }
function Captura($nombre) {
    Start-Sleep 2
    & $Adb shell screencap -p /sdcard/cap.png
    & $Adb pull /sdcard/cap.png (Join-Path $Salida "$nombre.png") | Out-Null
    Log "Captura: $nombre.png"
}
function Atras { & $Adb shell input keyevent 4; Start-Sleep 2 }

# ---------- 5. Recorrido ----------
& $Adb shell monkey -p com.leadlaunch -c android.intent.category.LAUNCHER 1 | Out-Null
if (Esperar "^Iniciar sesi" 60) { Captura "01_login" }

Tocar "^Correo electr" | Out-Null; Escribir "ana@dulcepunto.com"
Tocar "^Contrase" | Out-Null;      Escribir "Dulce123"
Ocultar-Teclado
Tocar "^Iniciar sesi" | Out-Null
Log "Login enviado (el servidor puede tardar ~50 s si estaba dormido)..."
if (Esperar "^Hola" 120) { Start-Sleep 6; Captura "02_inicio" }

if (Tocar "^Cat.logo$") { Esperar "Torta|TOR-001" 60 | Out-Null; Captura "03_catalogo" }
Tocar "^Inicio$" | Out-Null

if (Tocar "^Reglas de precios$") { Start-Sleep 5; Captura "04_reglas"; Atras }

if (Tocar "Escanear competencia \(Radar\)") {
    Esperar "^Escanear competencia$" 30 | Out-Null; Start-Sleep 4; Captura "05_radar"
    Tocar "^Escanear competencia$" | Out-Null
    if (Esperar "^Fricciones \(" 120) {
        Captura "06_hallazgos_fricciones"
        if (Tocar "^Competidores \(") { Captura "07_hallazgos_competidores" }
    }
    Atras; Atras
}

if (Tocar "^Campa.as$") { Start-Sleep 3; Captura "08_campanas_vista_previa" }
if (Tocar "^Cotizar$")  { Start-Sleep 3; Captura "09_cotizaciones_vista_previa" }
if (Tocar "^M.s$")      { Start-Sleep 3; Captura "10_perfil" }

& $Adb shell am broadcast -a com.android.systemui.demo -e command exit | Out-Null
Log "Fin: $(Get-Date). Capturas en $Salida"

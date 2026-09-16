$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
New-Item -ItemType Directory -Force -Path 'build/classes','build/test-classes','dist' | Out-Null
$javaBin = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { Split-Path (Get-Command javac.exe).Source }
$sources = Get-ChildItem -LiteralPath 'src/main/java' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
& (Join-Path $javaBin 'javac.exe') --release 17 -encoding UTF-8 -d build/classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
$tests = Get-ChildItem -LiteralPath 'src/test/java' -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName
& (Join-Path $javaBin 'javac.exe') --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes $tests
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
& (Join-Path $javaBin 'java.exe') -cp 'build/classes;build/test-classes' org.walks.rooms.RoomServerTest
if ($LASTEXITCODE -ne 0) { throw 'Server integration tests failed' }
& (Join-Path $javaBin 'java.exe') -cp 'build/classes;build/test-classes' org.walks.rooms.OneNightGameTest
if ($LASTEXITCODE -ne 0) { throw 'One-night rule tests failed' }
& (Join-Path $javaBin 'java.exe') -cp 'build/classes;build/test-classes' org.walks.rooms.CloudExpansionTest
if ($LASTEXITCODE -ne 0) { throw 'Cloud expansion tests failed' }
& (Join-Path $javaBin 'java.exe') -cp 'build/classes;build/test-classes' org.walks.rooms.CloudSixTest
if ($LASTEXITCODE -ne 0) { throw 'Avalon, drawing and browser tests failed' }
& (Join-Path $javaBin 'java.exe') -cp 'build/classes;build/test-classes' org.walks.rooms.RoomInvitationTest
if ($LASTEXITCODE -ne 0) { throw 'Room invitation and host view tests failed' }
& (Join-Path $javaBin 'jar.exe') --create --file dist/yigame-room-server.jar --main-class org.walks.rooms.RoomServer -C build/classes . -C src/main/resources .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
Write-Output 'Built dist/yigame-room-server.jar'

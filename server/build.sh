#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes build/test-classes dist
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt
find src/test/java -name '*.java' > build/tests.txt
javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @build/tests.txt
java -cp build/classes:build/test-classes org.walks.rooms.RoomServerTest
java -cp build/classes:build/test-classes org.walks.rooms.OneNightGameTest
java -cp build/classes:build/test-classes org.walks.rooms.CloudExpansionTest
java -cp build/classes:build/test-classes org.walks.rooms.CloudSixTest
java -cp build/classes:build/test-classes org.walks.rooms.RoomInvitationTest
jar --create --file dist/yigame-room-server.jar --main-class org.walks.rooms.RoomServer -C build/classes . -C src/main/resources .

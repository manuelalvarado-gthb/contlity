#!/bin/bash
# Compila el driver JDBC (Java 11+, sin dependencias) en dist/contlity-jdbc-VERSION.jar.
#   ./build.sh [VERSION]      por omisión, la versión del SDK de Python (../python/pyproject.toml)
# Requiere un JDK 11+ (javac y jar en el PATH).
set -euo pipefail
cd "$(dirname "$0")"
VER=${1:-$(sed -n 's/^version = "\(.*\)"/\1/p' ../python/pyproject.toml 2>/dev/null || true)}
VER=${VER:-0.0.0}
rm -rf build dist && mkdir -p build/clases dist
javac --release 11 -encoding UTF-8 -Xlint:-options -d build/clases $(find src/main/java -name '*.java')
cp -r src/main/resources/. build/clases/
echo "version=$VER" > build/clases/cloud/techmx/contlity/jdbc/version.properties
cat > build/MANIFEST.MF <<MF
Manifest-Version: 1.0
Automatic-Module-Name: cloud.techmx.contlity.jdbc
Main-Class: cloud.techmx.contlity.jdbc.ContlityDriver
Implementation-Title: Contlity JDBC
Implementation-Version: $VER
Implementation-Vendor: Contlity
MF
jar --create --file "dist/contlity-jdbc-$VER.jar" --manifest build/MANIFEST.MF -C build/clases .
cp "dist/contlity-jdbc-$VER.jar" dist/contlity-jdbc.jar
rm -rf build
ls -l dist

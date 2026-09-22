"""Verify a release APK, its manifest and its identity against the configured signing certificate."""
import argparse
import importlib.util
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

root = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('ci_android_release', root / 'scripts/ci-android-release.py')
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--apk', type=Path, required=True)
parser.add_argument('--output-dir', type=Path, required=True)
parser.add_argument('--channel', choices=release.CHANNELS, default='direct')
args = parser.parse_args()
apk = args.apk.resolve()
out = args.output_dir.resolve()
out.mkdir(parents=True, exist_ok=True)
java = Path(os.environ.get('JAVA_HOME', 'C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot')) / 'bin/java.exe'
sdk = Path(os.environ.get('ANDROID_HOME', 'D:/Develop/Android/SDK'))
build_tools = sdk / 'build-tools/36.1.0'


def run(arguments):
    result = subprocess.run([str(item) for item in arguments], capture_output=True, text=True, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout


with zipfile.ZipFile(apk) as archive:
    assert archive.testzip() is None, 'APK archive corruption'

signature = run([java, '-jar', build_tools / 'lib/apksigner.jar', 'verify', '--verbose', '--print-certs', apk])
(out / 'android-signature-check.log').write_text(signature, encoding='utf-8')
signer = release.certificate_digest(signature)
assert 'Verifies' in signature and 'DOES NOT VERIFY' not in signature

# Java reads the same Properties format as Gradle. Passwords never become command-line arguments or output.
with tempfile.TemporaryDirectory(prefix='release-certificate-') as directory:
    helper = Path(directory) / 'ReleaseCertificate.java'
    helper.write_text('''
import java.nio.file.*;
import java.security.*;
import java.util.*;
public class ReleaseCertificate {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Properties properties = new Properties();
        try (var input = Files.newInputStream(root.resolve("local.properties"))) { properties.load(input); }
        var file = root.resolve(properties.getProperty("KEYSTORE_FILE")).toFile();
        var store = KeyStore.getInstance(file, properties.getProperty("KEYSTORE_PASSWORD").toCharArray());
        var certificate = store.getCertificate(properties.getProperty("KEY_ALIAS"));
        System.out.println(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded())));
    }
}
''', encoding='utf-8')
    configured_certificate = run([java, helper, root]).strip()
assert signer == configured_certificate, 'APK signer differs from the configured release certificate'

manifest = run([build_tools / 'aapt2.exe', 'dump', 'badging', apk])
(out / 'android-manifest-check.log').write_text(manifest, encoding='utf-8')
package = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'", manifest)
assert package is not None
build = (root / 'composeApp/build.gradle.kts').read_text(encoding='utf-8')
tree = run([build_tools / 'aapt2.exe', 'dump', 'xmltree', apk, '--file', 'AndroidManifest.xml'])
release.verify_manifest(manifest, tree, args.channel)
assert package.group(2) == re.search(r'versionCode\s*=\s*(\d+)', build).group(1)
assert package.group(3) == re.search(r'versionName\s*=\s*"([^"]+)"', build).group(1) + ('-domestic' if args.channel == 'domestic' else '')
assert 'application-debuggable' not in manifest, 'APK is debuggable'
digest = hashlib.sha256(apk.read_bytes()).hexdigest()
result = {'channel': args.channel, 'roomsEnabled': args.channel != 'domestic',
          'packageName': package.group(1), 'versionName': package.group(3), 'versionCode': int(package.group(2)),
          'debuggable': False, 'signatureVerified': True, 'matchesReleaseCertificate': True,
          'signerCertificateSha256': signer, 'apkBytes': apk.stat().st_size, 'sha256': digest}
(out / 'android-package-check.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False))

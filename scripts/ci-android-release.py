"""Prepare and verify an Android release in Actions without logging signing secrets."""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys

SIGNING_KEYS = ('KEYSTORE_BASE64', 'KEYSTORE_PASSWORD', 'KEY_ALIAS', 'KEY_PASSWORD')


def property_value(value):
    """Encode exactly for Java Properties.load(InputStream), including Unicode."""
    escapes = {'\\': '\\\\', '\n': '\\n', '\r': '\\r', '\t': '\\t', '\f': '\\f', ' ': '\\ '}
    result = []
    for char in value:
        if char in escapes:
            result.append(escapes[char])
        elif ord(char) < 32 or ord(char) > 126:
            encoded = char.encode('utf-16-be')
            result.extend('\\u' + encoded[index:index + 2].hex() for index in range(0, len(encoded), 2))
        else:
            result.append(char)
    return ''.join(result)


def signing_dir():
    return Path(os.environ['RUNNER_TEMP']).resolve() / ('yigame-signing-' + os.environ['GITHUB_RUN_ID'] + '-' + os.environ['GITHUB_RUN_ATTEMPT'])


def prepare(root):
    if any(not os.environ.get(key) for key in SIGNING_KEYS):
        raise ValueError('All four Android signing secrets must be configured and nonempty.')
    properties = root / 'local.properties'
    if properties.exists():
        raise ValueError('Refusing to replace an existing local.properties file.')
    decoded = base64.b64decode(''.join(os.environ['KEYSTORE_BASE64'].split()), validate=True)
    if not decoded:
        raise ValueError('The configured keystore is empty.')
    directory = signing_dir()
    directory.mkdir(mode=0o700, parents=True, exist_ok=False)
    keystore = directory / 'release.keystore'
    keystore.write_bytes(decoded)
    keystore.chmod(0o600)
    values = {'KEYSTORE_FILE': str(keystore), **{key: os.environ[key] for key in SIGNING_KEYS[1:]}}
    # Mark ownership so cleanup cannot remove a pre-existing developer file.
    (directory / 'owns-properties').touch()
    properties.write_text(''.join(key + '=' + property_value(value) + '\n' for key, value in values.items()), encoding='ascii')
    properties.chmod(0o600)
    print('Signing configuration prepared; secret values are not logged.')


def command(arguments):
    result = subprocess.run([str(value) for value in arguments], capture_output=True, text=True)
    if result.returncode:
        # A signing tool could include user-provided values in diagnostics.
        raise ValueError('Release verification tool failed; no signing details were printed.')
    return result.stdout


def certificate_digest(signature):
    """Read the verified signer, accepting legacy and scheme-labelled SDK output."""
    if not re.search(r'^Verifies\s*$', signature, re.MULTILINE):
        raise ValueError('APK verification did not report success.')
    if not re.search(r'^Number of signers:\s*1\s*$', signature, re.MULTILINE):
        raise ValueError('Expected exactly one APK signer.')
    # Build-tools 37 labels certificates by scheme (e.g. "V2 Signer:").
    # Exclude public-key fingerprints and source-stamp certificates. Multiple
    # schemes may repeat one certificate, but different certificates must fail.
    certificates = {
        digest.lower() for digest in re.findall(
            r'^(?:Signer #\d+|V\d+(?:\.\d+)? Signer(?: #\d+)?):? certificate SHA-256 digest: ([0-9a-fA-F]{64})\s*$',
            signature, re.MULTILINE,
        )
    }
    if len(certificates) != 1:
        raise ValueError('Could not identify one consistent APK signing certificate.')
    return certificates.pop()


def verify(root):
    directory = root / 'composeApp/build/outputs/apk/release'
    metadata = json.loads((directory / 'output-metadata.json').read_text(encoding='utf-8'))
    elements = metadata['elements']
    if metadata.get('variantName') != 'release' or metadata.get('applicationId') != 'org.walks.gamecopilot' or len(elements) != 1:
        raise ValueError('Expected one release APK for org.walks.gamecopilot.')
    apk = (directory / elements[0]['outputFile']).resolve()
    if apk.parent != directory.resolve() or not apk.is_file() or apk.suffix != '.apk':
        raise ValueError('Release APK is missing or outside the expected output directory.')
    sdk = Path(os.environ['ANDROID_HOME'])
    candidates = [path for path in (sdk / 'build-tools').iterdir() if re.fullmatch(r'\d+\.\d+\.\d+', path.name)]
    tools = max(candidates, key=lambda path: tuple(int(value) for value in path.name.split('.')))
    java = Path(os.environ['JAVA_HOME']) / 'bin/java'
    print(f'Verifying release APK with Android build-tools {tools.name}.', flush=True)
    signature = command([java, '-jar', tools / 'lib/apksigner.jar', 'verify', '--verbose', '--print-certs', apk])
    actual = certificate_digest(signature)
    manifest = command([tools / 'aapt2', 'dump', 'badging', apk])
    if 'application-debuggable' in manifest or "package: name='org.walks.gamecopilot'" not in manifest:
        raise ValueError('Expected a non-debuggable production package.')
    # Read the exact same Java Properties file as Gradle; passwords never enter argv.
    helper = signing_dir() / 'ReleaseCertificate.java'
    helper.write_text('''
import java.nio.file.*;
import java.security.*;
import java.util.*;
class ReleaseCertificate {
  public static void main(String[] args) throws Exception {
    Properties p = new Properties();
    try (var stream = Files.newInputStream(Path.of(args[0]))) { p.load(stream); }
    var store = KeyStore.getInstance(Path.of(p.getProperty("KEYSTORE_FILE")).toFile(), p.getProperty("KEYSTORE_PASSWORD").toCharArray());
    var certificate = store.getCertificate(p.getProperty("KEY_ALIAS"));
    System.out.print(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded())));
  }
}
''', encoding='utf-8')
    expected = command([java, helper, root / 'local.properties']).strip().lower()
    if actual != expected:
        raise ValueError('APK signer does not match the configured release key.')
    report = {'applicationId': metadata['applicationId'], 'versionCode': elements[0]['versionCode'],
              'versionName': elements[0]['versionName'], 'debuggable': False, 'signatureVerified': True,
              'matchesReleaseCertificate': True, 'signerCertificateSha256': actual,
              'sha256': hashlib.sha256(apk.read_bytes()).hexdigest()}
    (root / 'release-verification.json').write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    with Path(os.environ['GITHUB_ENV']).open('a', encoding='utf-8') as stream:
        stream.write('RELEASE_APK_PATH=' + str(apk) + '\n')
    print('PASS: signed release APK, expected certificate and non-debuggable manifest.')


def cleanup(root):
    directory = signing_dir()
    if not directory.exists():
        return
    if (directory / 'owns-properties').exists():
        (root / 'local.properties').unlink(missing_ok=True)
    # Delete only the exact files created by this helper; no recursive deletion.
    for name in ('release.keystore', 'ReleaseCertificate.java', 'owns-properties'):
        (directory / name).unlink(missing_ok=True)
    directory.rmdir()
    print('Temporary signing files removed.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=('prepare', 'verify', 'cleanup'))
    args = parser.parse_args()
    if os.environ.get('GITHUB_ACTIONS') != 'true':
        raise ValueError('This helper is restricted to GitHub Actions.')
    root = Path(__file__).resolve().parents[1]
    {'prepare': prepare, 'verify': verify, 'cleanup': cleanup}[args.action](root)


if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError, KeyError) as error:
        # Known validation text is safe; filesystem paths and raw key diagnostics are omitted.
        print(str(error) if isinstance(error, ValueError) else 'CI signing operation failed.', file=sys.stderr)
        sys.exit(1)

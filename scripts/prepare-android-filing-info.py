"""Export public Android filing information after checking the APK release signer.

Only the public certificate/key are exported. Keystore passwords stay inside Java.
"""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--apk', type=Path, required=True)
parser.add_argument('--output-dir', type=Path, required=True)
parser.add_argument('--channel', choices=('domestic', 'googlePlay', 'direct', 'fdroid'), default='domestic')
args = parser.parse_args()
out = args.output_dir.resolve()
out.mkdir(parents=True, exist_ok=True)
subprocess.run([
    os.sys.executable, str(ROOT / 'scripts/verify-android-release.py'),
    '--apk', str(args.apk.resolve()), '--output-dir', str(out),
    '--channel', args.channel,
], check=True)
verified = json.loads((out / 'android-package-check.json').read_text(encoding='utf-8'))
java = Path(os.environ.get('JAVA_HOME', 'C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot')) / 'bin/java.exe'
with tempfile.TemporaryDirectory(prefix='android-filing-') as temporary:
    helper = Path(temporary) / 'PublicFilingInfo.java'
    helper.write_text('''
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
public class PublicFilingInfo {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Properties properties = new Properties();
        try (var input = Files.newInputStream(root.resolve("local.properties"))) { properties.load(input); }
        var file = root.resolve(properties.getProperty("KEYSTORE_FILE")).toFile();
        var store = KeyStore.getInstance(file, properties.getProperty("KEYSTORE_PASSWORD").toCharArray());
        var certificate = store.getCertificate(properties.getProperty("KEY_ALIAS"));
        var key = (RSAPublicKey) certificate.getPublicKey();
        System.out.println(Base64.getEncoder().encodeToString(certificate.getEncoded()));
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
        System.out.println(key.getModulus().toString(16));
        System.out.println(key.getPublicExponent().toString(10));
    }
}
''', encoding='utf-8')
    result = subprocess.run([str(java), str(helper), str(ROOT)], capture_output=True, text=True, check=True)
    certificate_b64, key_b64, modulus, exponent = result.stdout.strip().splitlines()
certificate = base64.b64decode(certificate_b64)
public_key = base64.b64decode(key_b64)
assert hashlib.sha256(certificate).hexdigest() == verified['signerCertificateSha256']


def pem(label, value):
    lines = '\n'.join(value[index:index + 64] for index in range(0, len(value), 64))
    return f'-----BEGIN {label}-----\n{lines}\n-----END {label}-----\n'


(out / 'android-release-certificate.cer').write_bytes(certificate)
(out / 'android-release-certificate.pem').write_text(pem('CERTIFICATE', certificate_b64), encoding='ascii')
(out / 'android-release-public-key.pem').write_text(pem('PUBLIC KEY', key_b64), encoding='ascii')
(out / 'android-public-key-modulus.txt').write_text(modulus.upper() + '\n', encoding='ascii')
info = {
    'appName': '桌游助手',
    'packageName': verified['packageName'],
    'versionName': verified['versionName'],
    'versionCode': verified['versionCode'],
    'certificateMd5': hashlib.md5(certificate).hexdigest().upper(),
    'certificateSha1': hashlib.sha1(certificate).hexdigest().upper(),
    'certificateSha256': hashlib.sha256(certificate).hexdigest().upper(),
    'publicKeyAlgorithm': 'RSA',
    'publicKeyModulusHex': modulus.upper(),
    'publicKeyExponent': int(exponent),
    'publicKeySpkiBase64': key_b64,
    'apkSha256': verified['sha256'],
    'note': '阿里云安卓公钥栏按官方说明填写 RSA 模数；MD5 栏填写证书 MD5，不能填写 APK MD5。未导出私钥。',
}
(out / 'android-filing-info.json').write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('Public filing certificate and RSA modulus exported; signer matches the APK.')

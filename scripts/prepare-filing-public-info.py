"""Export public filing reference data from the verified release certificate chain."""
import hashlib
import json
import re
import shutil
from pathlib import Path
from cryptography import x509
from cryptography.hazmat.primitives import serialization

root = Path(__file__).resolve().parents[1]
out = root / 'artifacts/roundtable-20260906'
icon = root.parent / 'YiGameCopilotX-Harmony/entry/src/main/resources/base/media/app_icon.png'
assert icon.stat().st_size < 100_000
shutil.copyfile(icon, out / 'filing-app-icon.png')
latest_release = root / 'artifacts/internal-test-20260908'
signing_source = latest_release if (latest_release / 'release-package-check.json').is_file() else out
release_info = json.loads((signing_source / 'release-package-check.json').read_text(encoding='utf-8'))
chain = (signing_source / 'release-verified-chain.cer').read_bytes()
certs = [x509.load_pem_x509_certificate(block) for block in re.findall(
    rb'-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----', chain, re.S)]
leaves = [cert for cert in certs if not cert.extensions.get_extension_for_class(x509.BasicConstraints).value.ca]
assert len(leaves) == 1, 'Expected exactly one app signing leaf certificate'
cert = leaves[0]
der = cert.public_bytes(serialization.Encoding.DER)
public_pem = cert.public_key().public_bytes(serialization.Encoding.PEM, serialization.PublicFormat.SubjectPublicKeyInfo)
public_der = cert.public_key().public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
(out / 'filing-harmony-release-public-key.pem').write_bytes(public_pem)
(out / 'filing-harmony-release-certificate.cer').write_bytes(cert.public_bytes(serialization.Encoding.PEM))
data = {
    'appName': '桌游助手',
    'bundleName': 'org.walks.gamecopilot',
    'platform': 'HarmonyOS',
    'version': f"{release_info['versionName']} ({release_info['versionCode']})",
    'source': str(signing_source / 'release-verified-chain.cer'),
    'certificateMD5': hashlib.md5(der).hexdigest().upper(),
    'certificateSHA256': hashlib.sha256(der).hexdigest().upper(),
    'publicKeyFormat': 'X.509 SubjectPublicKeyInfo DER (hex); verify field format with chosen filing provider',
    'publicKeyDERHex': public_der.hex().upper(),
    'android': 'Do not use the Android debug certificate for production filing; confirm final Android release signing first.',
    'filingSubmitted': False,
}
(out / 'APP_FILING_PUBLIC_INFO.json').write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')
print('Exported public Harmony release certificate, public key and filing reference JSON; no private key accessed.')

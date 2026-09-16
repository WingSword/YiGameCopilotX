"""Regression for SDK 36/37 signer output; optional real APK verification uses no private key."""
import argparse
import importlib.util
from pathlib import Path
import shutil
import sys
import tempfile
import unittest
import zipfile

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('ci_android_release', ROOT / 'scripts/ci-android-release.py')
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)
A = '0123456789abcdef' * 4
B = 'fedcba9876543210' * 4
REAL = None


def verified(*lines):
    return '\n'.join(['Verifies', 'Verified using v2 scheme (APK Signature Scheme v2): true',
                      'Number of signers: 1', *lines]) + '\n'


class SignerOutputTests(unittest.TestCase):
    def test_sdk_36_certificate(self):
        output = verified('Signer #1 certificate DN: CN=Test',
                          'Signer #1 certificate SHA-256 digest: ' + A,
                          'Signer #1 public key SHA-256 digest: ' + B)
        self.assertEqual(release.certificate_digest(output), A)

    def test_sdk_37_certificate(self):
        output = verified('V2 Signer: certificate DN: CN=Test',
                          'V2 Signer: certificate SHA-256 digest: ' + A,
                          'V2 Signer: public key SHA-256 digest: ' + B)
        self.assertEqual(release.certificate_digest(output), A)

    def test_same_certificate_in_multiple_schemes(self):
        output = verified('V2 Signer: certificate SHA-256 digest: ' + A,
                          'V3 Signer: certificate SHA-256 digest: ' + A.upper())
        self.assertEqual(release.certificate_digest(output), A)

    def test_different_scheme_certificates_are_rejected(self):
        with self.assertRaises(ValueError):
            release.certificate_digest(verified('V2 Signer: certificate SHA-256 digest: ' + A,
                                                'V3 Signer: certificate SHA-256 digest: ' + B))

    def test_public_keys_and_unrelated_certificates_are_rejected(self):
        for prefix in ('Signer #1 public key', 'V2 Signer: public key', 'Source Stamp Signer certificate', 'certificate'):
            with self.subTest(prefix=prefix), self.assertRaises(ValueError):
                release.certificate_digest(verified(prefix + ' SHA-256 digest: ' + A))

    def test_missing_and_malformed_digest_are_rejected(self):
        for output in (verified(), verified('Signer #1 certificate SHA-256 digest: ' + A[:63]),
                       verified('Signer #1 certificate SHA-256 digest: ' + A + '0'),
                       verified('Signer #1 certificate SHA-256 digest: ' + 'z' * 64),
                       verified('Signer #1 certificate SHA-256 digest: ' + A + ' trailing text')):
            with self.subTest(output=output), self.assertRaises(ValueError):
                release.certificate_digest(output)

    def test_verification_banner_and_single_signer_are_required(self):
        output = verified('Signer #1 certificate SHA-256 digest: ' + A)
        for changed in (output.replace('Verifies\n', ''), output.replace('Verifies\n', 'DOES NOT VERIFY\n'),
                        output.replace('Number of signers: 1', 'Number of signers: 2'),
                        output.replace('Number of signers: 1\n', '')):
            with self.subTest(output=changed), self.assertRaises(ValueError):
                release.certificate_digest(changed)

    def test_nonzero_tool_exit_is_rejected_even_with_success_text(self):
        program = "import sys; print(" + repr(verified('Signer #1 certificate SHA-256 digest: ' + A)) + "); sys.stderr.write('synthetic-secret-diagnostic'); sys.exit(7)"
        with self.assertRaises(ValueError) as raised:
            release.command([sys.executable, '-c', program])
        self.assertNotIn('synthetic-secret-diagnostic', str(raised.exception))


class RealApkTests(unittest.TestCase):
    def test_old_and_new_sdk_agree_and_tampered_apk_fails(self):
        if REAL is None:
            self.skipTest('provide --apk, --sdk and --java for real SDK checks')
        digests = []
        with tempfile.TemporaryDirectory(prefix='ci-apk-integrity-') as folder:
            tampered = Path(folder) / 'tampered.apk'
            shutil.copyfile(REAL.apk, tampered)
            with zipfile.ZipFile(tampered, 'a') as archive:
                archive.writestr('ci-integrity-probe.txt', b'modified after signing')
            for version in REAL.build_tools:
                jar = REAL.sdk / 'build-tools' / version / 'lib/apksigner.jar'
                prefix = [REAL.java, '-jar', jar, 'verify', '--verbose', '--print-certs']
                with self.subTest(build_tools=version):
                    result = release.command([*prefix, REAL.apk])
                    digests.append(release.certificate_digest(result))
                    with self.assertRaises(ValueError):
                        release.command([*prefix, tampered])
        self.assertEqual(len(set(digests)), 1, 'all SDKs must report the same actual certificate')


def main():
    global REAL
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', type=Path)
    parser.add_argument('--sdk', type=Path)
    parser.add_argument('--java', type=Path)
    parser.add_argument('--build-tools', action='append', default=[])
    args, remaining = parser.parse_known_args()
    if any((args.apk, args.sdk, args.java)):
        if not all((args.apk, args.sdk, args.java)):
            parser.error('--apk, --sdk and --java must be supplied together')
        args.build_tools = args.build_tools or ['36.1.0', '37.0.0']
        REAL = args
    unittest.main(argv=[sys.argv[0], *remaining])


if __name__ == '__main__':
    main()

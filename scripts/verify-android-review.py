"""Inspect a built APK for the release-tooling and credential-backup review fixes."""
import argparse
import json
from pathlib import Path
import re
import subprocess
import zipfile


def xmltree(apk, aapt2, resource):
    return subprocess.check_output(
        [str(aapt2), 'dump', 'xmltree', str(apk), '--file', resource],
        text=True, encoding='utf-8',
    )


def excludes_preferences(tree):
    nodes = re.findall(r'E: exclude\b(?:(?!\n\s*E:).)*', tree, re.DOTALL)
    return sum('domain="sharedpref"' in node and 'path="."' in node for node in nodes)


def rule_resource(table, name, manifest_attribute, manifest):
    match = re.search(r'resource (0x[0-9a-f]+) xml/' + name +
                      r'\s*\n\s*\(\) \(file\) (\S+) type=XML', table)
    if not match:
        raise ValueError(f'The packaged {name} resource is missing.')
    if not re.search(r'android:' + manifest_attribute + r'[^\n]*=@' + match[1], manifest):
        raise ValueError(f'The manifest does not reference the packaged {name} rules.')
    return match[2]


def verify(apk, aapt2):
    manifest = xmltree(apk, aapt2, 'AndroidManifest.xml')
    if 'androidx.compose.ui.tooling.PreviewActivity' in manifest:
        raise ValueError('Release APK still exposes Compose PreviewActivity.')
    if 'android:fullBackupContent' not in manifest or 'android:dataExtractionRules' not in manifest:
        raise ValueError('The APK must reference both Android backup-rule formats.')
    table = subprocess.check_output([str(aapt2), 'dump', 'resources', str(apk)], text=True, encoding='utf-8')
    legacy = xmltree(apk, aapt2, rule_resource(table, 'backup_rules', 'fullBackupContent', manifest))
    modern = xmltree(apk, aapt2, rule_resource(table, 'data_extraction_rules', 'dataExtractionRules', manifest))
    if 'E: full-backup-content' not in legacy or excludes_preferences(legacy) != 1:
        raise ValueError('Legacy backup rules do not exclude preferences.')
    for section in ('cloud-backup', 'device-transfer'):
        match = re.search(r'E: ' + section + r'\b(?:(?!\n\s*E: (?:cloud-backup|device-transfer)\b).)*', modern, re.DOTALL)
        if not match or excludes_preferences(match[0]) != 1:
            raise ValueError(f'{section} rules do not exclude preferences.')
    with zipfile.ZipFile(apk) as archive:
        for name in archive.namelist():
            if re.fullmatch(r'classes\d*\.dex', name):
                content = archive.read(name)
                if b'116.198.196.244' in content:
                    raise ValueError('The retired server address is still compiled into the APK.')
                if b'Landroidx/compose/ui/tooling/PreviewActivity;' in content:
                    raise ValueError('Compose PreviewActivity is still packaged in the APK.')
    return {'apk': str(apk), 'previewActivityAbsent': True,
            'preferencesExcludedFromLegacyBackup': True,
            'preferencesExcludedFromCloudBackup': True,
            'preferencesExcludedFromDeviceTransfer': True,
            'retiredEndpointAbsentFromDex': True}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apk', type=Path, required=True)
    parser.add_argument('--aapt2', type=Path, required=True)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    report = verify(args.apk.resolve(), args.aapt2.resolve())
    if args.output:
        args.output.write_text(json.dumps(report, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report, indent=2))

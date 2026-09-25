"""One entry point for generated UI/rule parity and tracking changes to native feature pairs.

This detects unreviewed source changes. It does NOT assert pixel or semantic equality of arbitrary pages.
"""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--harmony', type=Path, default=ROOT.parent / 'YiGameCopilotX-Harmony')
parser.add_argument('--sync', action='store_true', help='regenerate shared contracts and artwork before checking')
parser.add_argument('--record-review', nargs='+', metavar='FEATURE', help='record explicitly reviewed feature changes')
parser.add_argument('--note', help='review evidence or reason for a deliberate platform difference')
parser.add_argument('--init-tracking', action='store_true', help='capture initial file hashes, without claiming feature parity')
args = parser.parse_args()
if not args.harmony.is_dir():
    parser.error('Harmony project missing; supply --harmony with its checkout path.')
if args.record_review and not args.note:
    parser.error('--record-review requires --note describing verification or intentional differences')

for script in ['sync-harmony-design.py', 'sync-random-tools.py', 'sync-domestic-privacy.py', 'sync-full-privacy.py']:
    subprocess.run([sys.executable, str(ROOT/'scripts'/script), '--harmony', str(args.harmony)] + ([] if args.sync else ['--check']), check=True)
environment = os.environ.copy()
environment['HARMONY_PROJECT'] = str(args.harmony)
subprocess.run([sys.executable, str(ROOT/'scripts/test-random-parity.py')], env=environment, check=True)
subprocess.run([sys.executable, str(ROOT/'scripts/test-roundtable-parity.py')], env=environment, check=True)

features = json.loads((ROOT/'cross-platform/features.json').read_text(encoding='utf-8'))
tracking_path = ROOT/'cross-platform/review-state.json'
tracking = json.loads(tracking_path.read_text(encoding='utf-8')) if tracking_path.exists() else {}
if args.init_tracking and tracking:
    parser.error('Tracking already initialized; review named features instead of resetting all hashes.')
unknown = set(args.record_review or []) - features.keys()
if unknown:
    parser.error('Unknown feature(s): ' + ', '.join(sorted(unknown)))
current = {}
def source_hash(path):
    raw = path.read_bytes()
    if path.suffix.lower() not in {'.otf', '.ttf', '.png', '.webp', '.wasm'}:
        raw = raw.decode('utf-8-sig').replace('\r','').encode()
    return hashlib.sha256(raw).hexdigest()
for feature, platforms in features.items():
    current[feature] = {}
    for platform, patterns in platforms.items():
        base = ROOT if platform in {'android', 'web'} else args.harmony
        files = set()
        for pattern in patterns:
            found = {p for p in base.glob(pattern) if p.is_file()}
            if not found:
                parser.error(f'{feature}/{platform}: missing source pattern {pattern}; update feature mapping')
            files.update(found)
        current[feature][platform] = {
            p.relative_to(base).as_posix(): source_hash(p)
            for p in sorted(files)
        }
    if args.init_tracking or feature in (args.record_review or []):
        tracking[feature] = {
            'files': current[feature],
            'status': 'tracking-started' if args.init_tracking else 'reviewed',
            'note': 'Initial change-detection snapshot; existing UI/function differences are not certified.' if args.init_tracking else args.note,
            'date': datetime.datetime.now(datetime.timezone.utc).isoformat()
        }
if args.init_tracking or args.record_review:
    tracking_path.write_text(json.dumps(tracking, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

drift = []
for feature, platforms in current.items():
    previous = tracking.get(feature, {}).get('files', {})
    for platform, files in platforms.items():
        before = previous.get(platform, {})
        changed = [p for p in sorted(files.keys() | before.keys()) if files.get(p) != before.get(p)]
        if changed:
            drift.append(feature)
            print(f'REVIEW NEEDED [{feature}] {platform}:')
            for p in changed: print('  ' + p)
if drift:
    raise SystemExit('Review the corresponding implementation in both projects, then record the named feature with --record-review FEATURE --note "evidence". Source hashes only track review; they do not prove functional parity.')
print(f'PASS: generated UI/rules/artwork match; no unreviewed source changes in {len(features)} tracked feature groups.')

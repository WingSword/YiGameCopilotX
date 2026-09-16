"""Build the deployable ZIP from an explicit allowlist; exclude local data and credentials."""
from pathlib import Path
import argparse, hashlib, zipfile
root = Path(__file__).resolve().parents[1]
server = root/'server'
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--output-dir',type=Path,default=root/'artifacts/refinement-20260906')
args=parser.parse_args()
target = args.output_dir.resolve()/'yigame-room-server.zip'
target.parent.mkdir(parents=True,exist_ok=True)
files = [server/p for p in ['README.md','Dockerfile','compose.yaml','.dockerignore','.env.example','build.sh','build.ps1']]
files += sorted((server/'src').rglob('*.java')) + sorted((server/'deploy').glob('*'))
files += sorted(p for p in (server/'src/main/resources').rglob('*') if p.is_file())
jar = server/'dist/yigame-room-server.jar'
if not jar.is_file(): raise SystemExit('Run server/build.ps1 or build.sh first')
with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as archive:
    for p in files:
        name='yigame-room-server/'+p.relative_to(server).as_posix()
        info=zipfile.ZipInfo(name)
        info.external_attr=(0o100755 if p.suffix=='.sh' else 0o100644)<<16
        info.compress_type=zipfile.ZIP_DEFLATED
        archive.writestr(info,p.read_bytes().replace(b'\r\n',b'\n'))
    archive.write(jar,'yigame-room-server/yigame-room-server.jar')
with zipfile.ZipFile(target) as archive:
    assert archive.testzip() is None
    assert all('/data/' not in n and not n.endswith('/.env') for n in archive.namelist())
print(f'Packaged {target.name}: {target.stat().st_size} bytes, SHA256 {hashlib.sha256(target.read_bytes()).hexdigest()}')

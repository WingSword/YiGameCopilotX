"""Save non-secret admin statistics from the local QA server."""
import json, urllib.request
from pathlib import Path
out = Path(__file__).resolve().parents[1] / 'artifacts/refinement-20260906'
control = json.loads((out/'qa-server-control.json').read_text(encoding='utf-8'))
result = {}
for name in ('stats','rooms'):
    req = urllib.request.Request('http://127.0.0.1:18080/api/v1/admin/'+name,
                                 headers={'Authorization': 'Bearer '+control['admin']})
    with urllib.request.urlopen(req, timeout=5) as response: result[name] = json.load(response)
(out/'cloud-admin-verification.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print('Saved local server room counts and game statistics, without credentials.')

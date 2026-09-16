"""Serve the built Web client locally, proxying only game APIs to a chosen server.

This is a development preview, not a public deployment server. Logs exclude
queries, headers and bodies so membership credentials are never recorded.
"""
import argparse
import http.client
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--server', required=True)
parser.add_argument('--port', type=int, default=18085)
args = parser.parse_args()
upstream = urlsplit(args.server)
if upstream.scheme not in ('http', 'https') or not upstream.hostname or upstream.username or upstream.password or upstream.query or upstream.fragment:
    parser.error('server must be an HTTP(S) address without credentials, query or fragment')
root = Path(__file__).resolve().parents[1] / 'composeApp/build/dist/wasmJs/productionExecutable'
if not (root / 'index.html').is_file():
    parser.error('Build the production Web distribution first')


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(root), **kwargs)

    def log_message(self, format, *args):
        pass

    def end_headers(self):
        self.send_header('Cache-Control', 'no-store')
        super().end_headers()

    def proxy(self):
        if not self.path.startswith('/api/v1/rooms'):
            self.send_error(404)
            return
        connection = (http.client.HTTPSConnection if upstream.scheme == 'https' else http.client.HTTPConnection)(upstream.hostname, upstream.port, timeout=20)
        try:
            body = self.rfile.read(int(self.headers.get('Content-Length', '0')))
            headers = {key: value for key, value in self.headers.items()
                       if key.lower() in ('authorization', 'content-type', 'accept')}
            connection.request(self.command, upstream.path.rstrip('/') + self.path, body, headers)
            response = connection.getresponse()
            data = response.read()
            self.send_response(response.status)
            self.send_header('Content-Type', response.getheader('Content-Type', 'application/json'))
            self.send_header('Content-Length', str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        except (OSError, http.client.HTTPException):
            self.send_error(502, 'Game service unavailable')
        finally:
            connection.close()

    def do_GET(self):
        if self.path.startswith('/api/'):
            self.proxy()
        else:
            super().do_GET()

    do_POST = proxy
    do_DELETE = proxy


print(f'Web preview: http://127.0.0.1:{args.port}/; API: {args.server}', flush=True)
ThreadingHTTPServer(('127.0.0.1', args.port), Handler).serve_forever()

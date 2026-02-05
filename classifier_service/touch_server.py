from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs
import sqlite3

hostName = "localhost"
serverPort = 5555

# Ensure database and tables are set up
conn = sqlite3.connect('touches.db')
cur = conn.cursor()

cur.execute("""
    CREATE TABLE IF NOT EXISTS touch_data(
        user_id TEXT,
        session TEXT,
        csv_data TEXT
    );
""")
conn.commit()

class FingerprintServer(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        with open("touch.html") as index:
            self.wfile.write(bytes(index.read(), "utf-8"))

    def do_POST(self):
        length = int(self.headers.get('content-length'))
        data = parse_qs(self.rfile.read(length).decode('utf-8'))
        # Like: {'name': ['1'], 'fingerprint': ['{...}'], 'touch_csv': ['csv_string']}
        name = data.get('name', [''])[0]
        touch_csv = data.get('touch_csv', [''])[0]

        # Save fingerprint (overwrite if exists)
        # Save touch CSV (for demo, use session '1' -- expand if needed)
        if touch_csv:
            cur.execute("INSERT INTO touch_data(user_id, session, csv_data) VALUES (?, ?, ?);", (name, "1", touch_csv))
        conn.commit()

        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes(f"Success, thank you, {name}!", "utf-8"))

if __name__ == "__main__":        
    webServer = HTTPServer((hostName, serverPort), FingerprintServer)
    print("Server started http://%s:%s" % (hostName, serverPort))
    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass
    webServer.server_close()
    print("Server stopped.")

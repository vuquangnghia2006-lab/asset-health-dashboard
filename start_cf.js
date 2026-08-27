const { spawn } = require('child_process');
const fs = require('fs');
const path = 'C:\\Users\\vuqua\\OneDrive\\Documents\\Default Project\\cf_url.txt';
const proc = spawn('C:\\Users\\vuqua\\OneDrive\\Documents\\Default Project\\cloudflared.exe', ['tunnel', '--url', 'http://localhost:8080']);
const stream = fs.createWriteStream(path);
proc.stdout.pipe(stream);
proc.stderr.pipe(stream);
proc.on('exit', () => { stream.end(); process.exit(); });

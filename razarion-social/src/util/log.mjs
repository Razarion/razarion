// Deliberately colour-free: this runs in PowerShell, Git Bash and CI logs, and a plain prefix
// reads correctly in all three.
export const info = (msg) => console.log(msg);
export const step = (msg) => console.log('  > ' + msg);
export const ok = (msg) => console.log('[ok] ' + msg);
export const warn = (msg) => console.log('[!] ' + msg);
export const fail = (msg) => console.error('[fail] ' + msg);

// Upload progress on a single rewritten line; stays silent when the output is piped, because a
// log file full of carriage returns is worse than no progress at all.
export function progress(label, done, total) {
  if (!process.stdout.isTTY) return;
  const pct = total ? Math.floor((done / total) * 100) : 0;
  const mb = (n) => (n / 1024 / 1024).toFixed(1);
  process.stdout.write(`\r  ${label}: ${pct}% (${mb(done)} / ${mb(total)} MB)   `);
  if (done >= total) process.stdout.write('\n');
}

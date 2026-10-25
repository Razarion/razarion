// A Windows notification, for what the engine cannot fix alone - a network whose login ran out.
// Clicking it opens `url`, the dashboard. Nothing is installed for it: PowerShell's own app id
// may show toasts. Any failure is swallowed; a notification that does not appear must never stop
// the run that wanted to send it.

import { spawn } from 'node:child_process';

const APP_ID = '{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\\WindowsPowerShell\\v1.0\\powershell.exe';

const xml = (s) => String(s).replace(/[<>&"']/g, (c) => ({ '<': '&lt;', '>': '&gt;', '&': '&amp;', '"': '&quot;', "'": '&apos;' }[c]));

export function notify(title, message, url) {
  if (process.platform !== 'win32') return;
  const toast =
    `<toast activationType="protocol" launch="${xml(url)}"><visual><binding template="ToastGeneric">` +
    `<text>${xml(title)}</text><text>${xml(message)}</text></binding></visual>` +
    `<actions><action content="Open dashboard" activationType="protocol" arguments="${xml(url)}"/></actions></toast>`;
  const script = [
    '[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null',
    '[Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] | Out-Null',
    '$x = New-Object Windows.Data.Xml.Dom.XmlDocument',
    `$x.LoadXml('${toast.replace(/'/g, "''")}')`,
    `[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('${APP_ID}').Show([Windows.UI.Notifications.ToastNotification]::new($x))`,
  ].join('\n');
  try {
    const child = spawn('powershell', ['-NoProfile', '-NonInteractive', '-EncodedCommand', Buffer.from(script, 'utf16le').toString('base64')], {
      stdio: 'ignore', windowsHide: true,
    });
    child.on('error', () => {});
  } catch { /* see above */ }
}

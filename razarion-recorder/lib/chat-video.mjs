import { execFile } from 'node:child_process';
import { readFileSync, statSync } from 'node:fs';
import ffmpegPath from 'ffmpeg-static';

// The server takes at most 12 MB (ChatService.MAX_VIDEO_BYTES); a 50 s clip at these settings is ~5.
const MAX_BYTES = 11 * 1024 * 1024;

/**
 * The take made small for the chat: 540 wide, no sound, a capped bitrate, and the index at the
 * front (faststart) so it starts playing before it is all loaded. A take is several hundred MB;
 * a player in the Instagram app on a phone gets a few.
 */
export function chatClipArgs(take, out) {
  return ['-y', '-v', 'error', '-i', take, '-an',
    '-vf', 'scale=540:-2,fps=30',
    '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '30', '-maxrate', '900k', '-bufsize', '1800k',
    '-pix_fmt', 'yuv420p', '-profile:v', 'main', '-movflags', '+faststart', out];
}

function makeChatClip(take, out) {
  return new Promise((resolve, reject) => {
    execFile(ffmpegPath, chatClipArgs(take, out), { timeout: 5 * 60_000 }, (error, _stdout, stderr) => {
      if (error) reject(new Error(`ffmpeg: ${(stderr || error.message).toString().trim().slice(0, 300)}`));
      else resolve(out);
    });
  });
}

/**
 * A filmed moment into the chat: the portrait take made small and posted to the server, which
 * keeps it and announces it (POST /rest/recorder/chat/video). The chat shows only a button; the
 * clip loads when a player clicks it. What happened is kept on the moment (moment.chat).
 */
export async function momentToChat(moment, { post, make = makeChatClip, log }) {
  const take = moment.takes?.portrait ?? Object.values(moment.takes ?? {})[0];
  if (!take || !moment.userId) {
    moment.chat = { status: 'skipped', reason: !take ? 'no take' : 'no player' };
    return;
  }
  try {
    const out = take.replace(/(\.\w+)?$/, '-chat.mp4');
    await make(take, out);
    const size = statSync(out).size;
    if (size > MAX_BYTES) throw new Error(`chat clip is ${Math.round(size / 1048576)} MB, more than the server takes`);
    const videoId = await post(moment.userId, readFileSync(out));
    moment.chat = { status: 'posted', videoId, bytes: size, at: Date.now() };
    log(`moment ${moment.id}: in the chat as video ${videoId} (${(size / 1048576).toFixed(1)} MB)`);
  } catch (error) {
    moment.chat = { status: 'failed', error: error.message };
    log(`moment ${moment.id}: not in the chat, ${error.message}`);
  }
}

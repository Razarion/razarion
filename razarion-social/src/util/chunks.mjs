import { open } from 'node:fs/promises';

/**
 * Reads a file in fixed-size slices. Videos here run to hundreds of megabytes, so nothing in
 * this tool ever holds a whole clip in memory - every upload path is a loop over these.
 */
export async function* fileChunks(path, chunkSize) {
  const fh = await open(path, 'r');
  try {
    const buffer = Buffer.alloc(chunkSize);
    let offset = 0;
    while (true) {
      const { bytesRead } = await fh.read(buffer, 0, chunkSize, offset);
      if (bytesRead === 0) return;
      yield { chunk: buffer.subarray(0, bytesRead), start: offset, end: offset + bytesRead - 1 };
      offset += bytesRead;
    }
  } finally {
    await fh.close();
  }
}

export async function readWhole(path) {
  const fh = await open(path, 'r');
  try {
    return await fh.readFile();
  } finally {
    await fh.close();
  }
}

/** Reads exactly `length` bytes starting at `start`. Used where the API dictates the ranges. */
export async function readRange(path, start, length) {
  const fh = await open(path, 'r');
  try {
    const buffer = Buffer.alloc(length);
    const { bytesRead } = await fh.read(buffer, 0, length, start);
    return buffer.subarray(0, bytesRead);
  } finally {
    await fh.close();
  }
}

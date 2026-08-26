import { createHash, createHmac } from 'node:crypto';
import { env, requireEnv } from '../../src/config.mjs';

// Cloudflare R2 speaks S3, and S3 only needs one signed PUT per file. The AWS SDK is 30 MB of
// dependency for a request that is 40 lines of HMAC, so it is written out here instead.
const SERVICE = 's3';
const REGION = 'auto';

const sha256 = (data) => createHash('sha256').update(data).digest('hex');
const hmac = (key, data) => createHmac('sha256', key).update(data).digest();

function amzDates() {
  const now = new Date().toISOString().replace(/[:-]|\.\d{3}/g, '');
  return { amzDate: now, dateStamp: now.slice(0, 8) };
}

function encodeKey(key) {
  return key
    .split('/')
    .map((segment) => encodeURIComponent(segment))
    .join('/');
}

export function r2Config() {
  const [accountId, bucket, accessKey, secretKey, publicBase] = requireEnv(
    'R2_ACCOUNT_ID',
    'R2_BUCKET',
    'R2_ACCESS_KEY_ID',
    'R2_SECRET_ACCESS_KEY',
    'R2_PUBLIC_BASE_URL'
  );
  return {
    accountId,
    bucket,
    accessKey,
    secretKey,
    publicBase: publicBase.replace(/\/+$/, ''),
    prefix: (env.R2_PREFIX || 'x-backfill').replace(/^\/+|\/+$/g, ''),
    host: `${accountId}.r2.cloudflarestorage.com`,
  };
}

/**
 * Signs and sends a single PUT.
 *
 * Instagram fetches the file itself from a public URL, so the object has to be readable without a
 * signature afterwards. That is a bucket setting in Cloudflare (r2.dev subdomain or a custom
 * domain), not something this request can ask for - see the README.
 */
export async function putObject(config, key, body, contentType) {
  const { amzDate, dateStamp } = amzDates();
  const fullKey = config.prefix ? `${config.prefix}/${key}` : key;
  const canonicalUri = '/' + encodeKey(config.bucket) + '/' + encodeKey(fullKey);
  const payloadHash = sha256(body);

  const headers = {
    host: config.host,
    'content-type': contentType,
    'x-amz-content-sha256': payloadHash,
    'x-amz-date': amzDate,
  };
  const signedHeaders = Object.keys(headers).sort().join(';');
  const canonicalHeaders = Object.keys(headers)
    .sort()
    .map((h) => `${h}:${headers[h]}\n`)
    .join('');

  const canonicalRequest = [
    'PUT',
    canonicalUri,
    '',
    canonicalHeaders,
    signedHeaders,
    payloadHash,
  ].join('\n');

  const scope = `${dateStamp}/${REGION}/${SERVICE}/aws4_request`;
  const stringToSign = ['AWS4-HMAC-SHA256', amzDate, scope, sha256(canonicalRequest)].join('\n');

  const signingKey = ['AWS4' + config.secretKey, dateStamp, REGION, SERVICE, 'aws4_request'].reduce(
    (key, part) => hmac(key, part)
  );
  const signature = createHmac('sha256', signingKey).update(stringToSign).digest('hex');

  const authorization =
    `AWS4-HMAC-SHA256 Credential=${config.accessKey}/${scope}, ` +
    `SignedHeaders=${signedHeaders}, Signature=${signature}`;

  const res = await fetch(`https://${config.host}${canonicalUri}`, {
    method: 'PUT',
    headers: { ...headers, authorization },
    body,
  });

  if (!res.ok) {
    const detail = await res.text();
    throw new Error(`R2 PUT ${fullKey} failed: HTTP ${res.status}\n${detail}`);
  }

  return { key: fullKey, url: `${config.publicBase}/${fullKey}` };
}

const CONTENT_TYPES = {
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.png': 'image/png',
  '.mp4': 'video/mp4',
};

export function contentTypeFor(file) {
  const ext = file.slice(file.lastIndexOf('.')).toLowerCase();
  return CONTENT_TYPES[ext] || 'application/octet-stream';
}

export { sha256 };

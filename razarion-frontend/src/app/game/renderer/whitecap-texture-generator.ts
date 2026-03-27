/**
 * Generates a procedural whitecap/foam texture on a canvas.
 * Creates organic-looking foam patches with bubbly structure and soft edges.
 * Returns a data URL suitable for Babylon.js Texture.
 */
export function generateWhitecapTexture(size: number = 256): string {
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d')!;

  // Start with transparent black
  ctx.clearRect(0, 0, size, size);

  // Seed-based pseudo-random for reproducibility
  let seed = 12345;
  function rand(): number {
    seed = (seed * 16807 + 0) % 2147483647;
    return (seed - 1) / 2147483646;
  }

  // Draw multiple foam clusters
  const clusterCount = 12;
  for (let c = 0; c < clusterCount; c++) {
    const cx = rand() * size;
    const cy = rand() * size;
    const clusterRadius = 15 + rand() * 30;
    const bubbleCount = 30 + Math.floor(rand() * 50);

    for (let b = 0; b < bubbleCount; b++) {
      // Place bubbles around cluster center with gaussian-like distribution
      const angle = rand() * Math.PI * 2;
      const dist = rand() * rand() * clusterRadius;
      const bx = cx + Math.cos(angle) * dist;
      const by = cy + Math.sin(angle) * dist;
      const radius = 1 + rand() * 4;
      const alpha = 0.3 + rand() * 0.7;

      // Draw bubble with soft edge
      const gradient = ctx.createRadialGradient(bx, by, 0, bx, by, radius);
      gradient.addColorStop(0, `rgba(255, 255, 255, ${alpha})`);
      gradient.addColorStop(0.5, `rgba(240, 245, 255, ${alpha * 0.6})`);
      gradient.addColorStop(1, `rgba(230, 240, 255, 0)`);

      ctx.beginPath();
      ctx.arc(bx, by, radius, 0, Math.PI * 2);
      ctx.fillStyle = gradient;
      ctx.fill();
    }

    // Add some thin streaky lines within the cluster for foam structure
    const streakCount = 3 + Math.floor(rand() * 5);
    for (let s = 0; s < streakCount; s++) {
      const sx = cx + (rand() - 0.5) * clusterRadius * 1.5;
      const sy = cy + (rand() - 0.5) * clusterRadius * 1.5;
      const length = 5 + rand() * 15;
      const angle = rand() * Math.PI * 2;

      ctx.beginPath();
      ctx.moveTo(sx, sy);
      ctx.lineTo(sx + Math.cos(angle) * length, sy + Math.sin(angle) * length);
      ctx.strokeStyle = `rgba(255, 255, 255, ${0.2 + rand() * 0.4})`;
      ctx.lineWidth = 0.5 + rand() * 1.5;
      ctx.stroke();
    }
  }

  // Add scattered individual tiny bubbles across the whole texture
  for (let i = 0; i < 200; i++) {
    const x = rand() * size;
    const y = rand() * size;
    const r = 0.5 + rand() * 1.5;
    const a = 0.15 + rand() * 0.35;

    ctx.beginPath();
    ctx.arc(x, y, r, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255, 255, 255, ${a})`;
    ctx.fill();
  }

  return canvas.toDataURL();
}

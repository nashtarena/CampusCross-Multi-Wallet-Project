const BASE = import.meta.env.VITE_API_BASE || '/api/v1';

export async function health() {
  const r = await fetch(`${BASE}/health`, { credentials: 'include' });
  return r.json();
}

// example: transfer
export async function p2pTransfer(body) {
  const r = await fetch(`${BASE}/transfer/p2p`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    credentials: 'include'
  });
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}

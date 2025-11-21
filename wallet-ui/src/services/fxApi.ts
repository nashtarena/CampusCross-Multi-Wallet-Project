const FX_API_BASE_URL = "https://fx-service.onrender.com";

export interface QuoteResponse {
  from: string;
  to: string;
  rate: number;
}

export const fxApi = {
  getQuote: async (from: string, to: string): Promise<QuoteResponse> => {
    const url = `${FX_API_BASE_URL}/quote/${encodeURIComponent(from)}/${encodeURIComponent(to)}`;
    const res = await fetch(url);
    if (!res.ok) {
      const txt = await res.text();
      throw new Error(`Failed to fetch FX quote: ${res.status} ${txt}`);
    }
    return res.json();
  }
};

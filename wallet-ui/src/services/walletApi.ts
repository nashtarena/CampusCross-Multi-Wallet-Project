const WALLET_API_BASE_URL = import.meta.env.VITE_WALLET_API_URL || 'http://localhost:8085/api';

// Helper function to get headers with JWT token
const getAuthHeaders = () => {
  const token = localStorage.getItem('authToken');
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
  };
};

// Types for wallet service
export interface User {
  id: number;
  username: string;
  email: string;
  phone: string;
  fullName: string;
  emailVerified: boolean;
  createdAt: string;
}

export interface Wallet {
  id: number;
  userId: number;
  walletAddress: string;
  walletName?: string;
  type: string;
  status: string;
  balance: number;
  currencyCode: string;
  currency: string;
  isDefault: boolean;
  dailyLimit: number;
  monthlyLimit: number;
  isFrozen: boolean;
  isClosed: boolean;
}

export interface Transaction {
  id: string;
  transactionId: string;
  type: 'P2P_TRANSFER' | 'CAMPUS_PAYMENT' | 'REMITTANCE' | 'ADD_FUNDS' | 'DEDUCT_FUNDS';
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  amount: number;
  currencyCode: string;
  currency: string;
  description: string;
  createdAt: string;
  completedAt?: string;
  message?: string;
  exchangeRate?: number;
  originalAmount?: number;
  originalCurrency?: string;
  referenceId?: string;
  merchantId?: string;
  campusLocation?: string;
  externalTransactionId?: string;
  feeAmount?: number;
  feeCurrency?: string;
  processingTimeMs?: number;
  failureReason?: string;
  ipAddress?: string;
  deviceFingerprint?: string;
  flagged?: boolean;
  flagReason?: string;
  recipient?: string;
}

export interface AuthRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  studentId: string;
  campusName?: string;
  role?: string;
}

export interface AuthResponse {
  userId: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
  kycStatus?: string;
  message: string;
  token: string;
}

export interface P2PTransferRequest {
  fromWalletId: number;
  toWalletAddress: string;
  amount: number;
  currency: string;
  description?: string;
}

export interface CampusPaymentRequest {
  walletId: number;
  merchantId: string;
  amount: number;
  currency: string;
  description?: string;
}

export interface RemittanceRequest {
  walletId: number;
  recipientName: string;
  recipientAccount: string;
  recipientBank: string;
  routingNumber?: string;
  amount: number;
  currency: string;
  description?: string;
}

// Authentication API
export const authApi = {
  register: async (userData: AuthRequest): Promise<AuthResponse> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Registration failed: ${errorText}`);
    }

    return response.json();
  },

  login: async (studentId: string, password: string, role?: string): Promise<AuthResponse> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ studentId, password, role }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Login failed: ${errorText}`);
    }

    return response.json();
  },

  verifyEmail: async (token: string): Promise<void> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/auth/verify-email`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ token }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Email verification failed: ${errorText}`);
    }
  },

  resetPassword: async (email: string): Promise<void> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/auth/reset-password`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ email }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Password reset failed: ${errorText}`);
    }
  },
};

// Wallet API
export const walletApi = {
  createWallet: async (walletName: string = 'Default Wallet', currency: string = 'USD', isDefault: boolean = true): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/create`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ 
        walletName,
        type: 'PERSONAL',
        currencyCode: currency,
        isDefault
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to create wallet: ${errorText}`);
    }

    return response.json();
  },

  getUserWallets: async (userId: number): Promise<Wallet[]> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/user/${userId}`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch wallets: ${errorText}`);
    }

    return response.json();
  },

  getDefaultWallet: async (userId: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/user/${userId}/default`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch default wallet: ${errorText}`);
    }

    return response.json();
  },

  getTotalBalance: async (userId: number): Promise<{ totalBalance: number; currency: string }> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/user/${userId}/total-balance`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch total balance: ${errorText}`);
    }

    return response.json();
  },

deleteWallet: async (walletId: number): Promise<void> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to delete wallet: ${errorText}`);
    }
  },
  addFunds: async (walletId: number, amount: number, currency: string): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/add-funds`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, currency }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to add funds: ${errorText}`);
    }

    return response.json();
  },

  /**
   * Call the simulated banking deposit endpoint so a transaction record is created
   * and funds are added to the user's default wallet via backend BankingService.
   */
  depositFromBank: async (userId: string | number, amount: number, currency: string): Promise<any> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/banking/deposit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ userId: String(userId), amount, currency }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to deposit from bank: ${errorText}`);
    }

    return response.json();
  },

  deductFunds: async (walletId: number, amount: number, currency: string): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/deduct-funds`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, currency }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to deduct funds: ${errorText}`);
    }

    return response.json();
  },

  freezeWallet: async (walletId: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/freeze`, {
      method: 'POST',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to freeze wallet: ${errorText}`);
    }

    return response.json();
  },

  unfreezeWallet: async (walletId: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/unfreeze`, {
      method: 'POST',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to unfreeze wallet: ${errorText}`);
    }

    return response.json();
  },

  closeWallet: async (walletId: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/close`, {
      method: 'POST',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to close wallet: ${errorText}`);
    }

    return response.json();
  },

  setDailyLimit: async (walletId: number, limit: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/daily-limit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ limit }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to set daily limit: ${errorText}`);
    }

    return response.json();
  },

  setMonthlyLimit: async (walletId: number, limit: number): Promise<Wallet> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/wallets/${walletId}/monthly-limit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ limit }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to set monthly limit: ${errorText}`);
    }

    return response.json();
  },
};

// Transaction API
export const transactionApi = {
  p2pTransfer: async (transferData: P2PTransferRequest): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/p2p-transfer`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(transferData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`P2P transfer failed: ${errorText}`);
    }

    return response.json();
  },

  campusPayment: async (paymentData: CampusPaymentRequest): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/campus-payment`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(paymentData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Campus payment failed: ${errorText}`);
    }

    return response.json();
  },

  remittance: async (remittanceData: RemittanceRequest): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/remittance`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(remittanceData),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Remittance failed: ${errorText}`);
    }

    return response.json();
  },

  getUserTransactions: async (userId: number): Promise<Transaction[]> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/user/${userId}`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch transactions: ${errorText}`);
    }

    return response.json();
  },

  getTransactionsByDateRange: async (
    userId: number,
    startDate: string,
    endDate: string
  ): Promise<Transaction[]> => {
    const response = await fetch(
      `${WALLET_API_BASE_URL}/transactions/user/${userId}/date-range?startDate=${startDate}&endDate=${endDate}`
    );

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch transactions by date range: ${errorText}`);
    }

    return response.json();
  },

  getTransaction: async (transactionId: number): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/${transactionId}`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch transaction: ${errorText}`);
    }

    return response.json();
  },

  refundTransaction: async (transactionId: number): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/${transactionId}/refund`, {
      method: 'POST',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to refund transaction: ${errorText}`);
    }

    return response.json();
  },

  cancelTransaction: async (transactionId: number): Promise<Transaction> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/${transactionId}/cancel`, {
      method: 'POST',
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to cancel transaction: ${errorText}`);
    }

    return response.json();
  },

  getFlaggedTransactions: async (): Promise<Transaction[]> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/flagged`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch flagged transactions: ${errorText}`);
    }

    return response.json();
  },

  getFailedTransactions: async (): Promise<Transaction[]> => {
    const response = await fetch(`${WALLET_API_BASE_URL}/transactions/failed`, {
      headers: getAuthHeaders()
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch failed transactions: ${errorText}`);
    }

    return response.json();
  },
};

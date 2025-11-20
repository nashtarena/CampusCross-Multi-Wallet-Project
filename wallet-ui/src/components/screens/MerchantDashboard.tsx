import React, { useEffect, useState } from 'react';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Badge } from '../ui/badge';
import { Bell, Settings, LogOut, Send, QrCode, Clock } from 'lucide-react';
import { useAppContext } from '../../App';
import { walletApi, transactionApi, Wallet, Transaction } from '../../services/walletApi';
import { NotificationsPanel } from '../notifications/NotificationsPanel';
import { toast } from 'sonner';

interface MerchantDashboardProps {
  onNavigate: (screen: string) => void;
}

export function MerchantDashboard({ onNavigate }: MerchantDashboardProps) {
  const { theme, toggleTheme, logout, userName } = useAppContext();
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingTx, setIsLoadingTx] = useState(true);
  const [showNotifications, setShowNotifications] = useState(false);

  const displayName = userName || 'Merchant';
  const initials = displayName
    .split(' ')
    .map((n: string) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  const getUserId = () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    try {
      const user = JSON.parse(userStr);
      return user.id;
    } catch {
      return null;
    }
  };

  const getBusinessCountry = () => {
    const userStr = localStorage.getItem('user');
    if (!userStr) return undefined;
    try {
      const user = JSON.parse(userStr);
      // Country is not in AuthResponse yet, but we may add it later
      return (user.country as string | undefined) || undefined;
    } catch {
      return undefined;
    }
  };

  const getCurrencySymbol = (currency: string) => {
    const symbols: { [key: string]: string } = {
      USD: '$',
      EUR: '€',
      GBP: '£',
      JPY: '¥',
      NGN: '₦',
      KES: 'KSh',
      GHS: '₵',
      ZAR: 'R',
      CAD: 'C$',
      AUD: 'A$',
      CHF: 'Fr',
      CNY: '¥',
      INR: '₹',
    };
    return symbols[currency] || currency;
  };

  useEffect(() => {
    const loadData = async () => {
      const userId = getUserId();
      if (!userId) {
        setIsLoading(false);
        setIsLoadingTx(false);
        return;
      }
      try {
        setIsLoading(true);
        setIsLoadingTx(true);

        const defaultWallet = await walletApi.getDefaultWallet(Number(userId));
        setWallet(defaultWallet);

        const tx = await transactionApi.getUserTransactions(Number(userId));
        const txArray = Array.isArray(tx)
          ? tx
          : tx && typeof tx === 'object' && 'transactions' in (tx as any)
          ? ((tx as any).transactions as Transaction[])
          : tx && typeof tx === 'object' && 'content' in (tx as any)
          ? ((tx as any).content as Transaction[])
          : [];
        setRecentTransactions(txArray.slice(0, 5));
      } catch (error: any) {
        console.error('Failed to load merchant data', error);
        toast.error('Failed to load merchant data');
      } finally {
        setIsLoading(false);
        setIsLoadingTx(false);
      }
    };

    loadData();
  }, []);

  const themeBg = theme === 'dark' ? 'bg-gray-900' : 'bg-slate-50';
  const cardBg = theme === 'dark' ? 'bg-gray-800' : 'bg-white';
  const textColor = theme === 'dark' ? 'text-gray-100' : 'text-gray-900';
  const textSecondary = theme === 'dark' ? 'text-gray-400' : 'text-gray-600';

  const businessCountry = getBusinessCountry();

  const handleCopyAddress = async () => {
    if (!wallet) return;
    try {
      await navigator.clipboard.writeText(wallet.walletAddress);
      toast.success('Wallet address copied');
    } catch {
      toast.error('Failed to copy address');
    }
  };

  const formatDate = (iso: string) => {
    if (!iso) return '';
    const date = new Date(iso);
    return date.toLocaleString();
  };

  const isCreditTransaction = (tx: Transaction) => {
    // Simple heuristic: positive amount is credit
    return tx.amount >= 0;
  };

  return (
    <div className={`min-h-screen ${themeBg} flex flex-col`}>
      {/* Header */}
      <div className="px-6 pt-8 pb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Avatar className="w-10 h-10">
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
          <div>
            <p className={`text-sm ${textSecondary}`}>Welcome back</p>
            <h1 className={`text-lg font-semibold ${textColor}`}>{displayName}</h1>
            <p className="text-xs text-emerald-500 font-medium">Merchant Dashboard</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowNotifications(true)}
            className="w-9 h-9 rounded-full flex items-center justify-center bg-white shadow-sm"
          >
            <Bell size={18} className="text-gray-700" />
          </button>
          <button
            onClick={toggleTheme}
            className="w-9 h-9 rounded-full flex items-center justify-center bg-white shadow-sm"
          >
            <Settings size={18} className="text-gray-700" />
          </button>
          <button
            onClick={logout}
            className="w-9 h-9 rounded-full flex items-center justify-center bg-white shadow-sm"
          >
            <LogOut size={18} className="text-red-500" />
          </button>
        </div>
      </div>

      {/* Wallet Summary */}
      <div className="px-6 mb-4">
        <Card className={`p-4 rounded-2xl shadow-md ${cardBg}`}>
          {wallet ? (
            <>
              <div className="flex items-center justify-between mb-2">
                <div>
                  <p className={`text-xs uppercase tracking-wide ${textSecondary}`}>Business Wallet</p>
                  <h2 className={`text-2xl font-semibold ${textColor}`}>
                    {getCurrencySymbol(wallet.currencyCode)}{wallet.balance.toFixed(2)}
                  </h2>
                </div>
                <Badge className="bg-emerald-100 text-emerald-700 border-emerald-200 text-xs">
                  {wallet.currencyCode}
                </Badge>
              </div>
              <p className={`text-xs ${textSecondary} mb-1`}>
                Wallet Address
              </p>
              <div className="flex items-center justify-between gap-3">
                <p className="text-xs font-mono break-all text-gray-500 flex-1">
                  {wallet.walletAddress}
                </p>
                <Button size="sm" variant="outline" className="text-xs" onClick={handleCopyAddress}>
                  Copy
                </Button>
              </div>
              {businessCountry && (
                <p className={`text-xs mt-2 ${textSecondary}`}>
                  Country of business: <span className="font-medium">{businessCountry}</span>
                </p>
              )}
              <p className="text-[11px] text-amber-500 mt-2">
                This wallet accepts payments only in your business currency.
              </p>
            </>
          ) : isLoading ? (
            <p className={textSecondary}>Loading wallet...</p>
          ) : (
            <p className={textSecondary}>No wallet found for this merchant.</p>
          )}
        </Card>
      </div>

      {/* Quick Actions */}
      <div className="px-6 mb-4">
        <Card className={`p-4 rounded-2xl shadow-md ${cardBg}`}>
          <div className="flex items-center justify-between mb-3">
            <p className={textColor}>Actions</p>
          </div>
          <div className="grid grid-cols-3 gap-3 text-center">
            <button
              onClick={handleCopyAddress}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50"
            >
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center">
                <QrCode className="text-white" size={18} />
              </div>
              <span className="text-xs text-gray-700">Accept Payment</span>
            </button>
            <button
              onClick={() => onNavigate('p2p')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50"
            >
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-400 to-cyan-600 flex items-center justify-center">
                <Send className="text-white" size={18} />
              </div>
              <span className="text-xs text-gray-700">P2P Transfer</span>
            </button>
            <button
              onClick={() => onNavigate('remittance')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50"
            >
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center">
                <Send className="text-white" size={18} />
              </div>
              <span className="text-xs text-gray-700">Remittance</span>
            </button>
          </div>
          <p className="text-[11px] text-gray-400 mt-2">
            P2P and remittance will use your primary business wallet and currency.
          </p>
        </Card>
      </div>

      {/* Recent Transactions */}
      <div className="px-6 pb-6 flex-1 overflow-auto">
        <div className="flex items-center justify-between mb-3">
          <p className={textColor}>Recent Activity</p>
        </div>
        <Card className={`divide-y border-0 shadow-md ${cardBg}`}>
          {recentTransactions.length > 0 ? (
            recentTransactions.map((tx) => (
              <div key={tx.transactionId} className="p-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div
                    className={`w-10 h-10 rounded-full flex items-center justify-center ${
                      isCreditTransaction(tx)
                        ? 'bg-green-100'
                        : 'bg-red-100'
                    }`}
                  >
                    <Clock
                      className={isCreditTransaction(tx) ? 'text-green-600' : 'text-red-600'}
                      size={18}
                    />
                  </div>
                  <div>
                    <p className={`text-sm ${textColor}`}>{tx.description || tx.type}</p>
                    <p className={`text-xs ${textSecondary}`}>{tx.type}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p
                    className={`text-sm ${
                      isCreditTransaction(tx) ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {isCreditTransaction(tx) ? '+' : '-'}
                    {getCurrencySymbol(tx.currencyCode)}
                    {Math.abs(tx.amount).toFixed(2)}
                  </p>
                  <p className={`text-xs ${textSecondary}`}>{formatDate(tx.createdAt)}</p>
                </div>
              </div>
            ))
          ) : isLoadingTx ? (
            <div className="text-center py-8">
              <p className={textSecondary}>Loading transactions...</p>
            </div>
          ) : (
            <div className="text-center py-8">
              <p className={textSecondary}>No recent transactions</p>
            </div>
          )}
        </Card>
      </div>

      {/* Notifications Panel */}
      <NotificationsPanel
        isOpen={showNotifications}
        onClose={() => setShowNotifications(false)}
        userId={getUserId() || ''}
      />
    </div>
  );
}

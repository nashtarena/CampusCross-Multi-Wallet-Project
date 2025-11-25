import React, { useState, useEffect } from 'react';
import { WalletCard } from '../wallet/WalletCard';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Badge } from '../ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { toast } from 'sonner';
import { Switch } from '../ui/switch';
import { Label } from '../ui/label';
import { useAppContext } from '../../App';
import { walletApi, transactionApi } from '../../services/walletApi';
import { Wallet, Transaction } from '../../services/walletApi';
import { NotificationsPanel } from '../notifications/NotificationsPanel';
import { Bell, Settings, Eye, EyeOff, Send, QrCode, ArrowLeftRight, AlertCircle as AlertCircleIcon, Plus, Clock, LogOut } from 'lucide-react';

interface HomeProps {
  onNavigate: (screen: string) => void;
}

const currencies = [
  { currency: 'USD', symbol: '$', balance: 2450.75, color: '#2ECC71' },
  { currency: 'EUR', symbol: '€', balance: 1820.50, color: '#9B59B6' },
  { currency: 'GBP', symbol: '£', balance: 980.25, color: '#E67E22' },
  { currency: 'JPY', symbol: '¥', balance: 125000, color: '#E74C3C' },
  { currency: 'INR', symbol: '₹', balance: 45500, color: '#F4C542' }
];

const recentTransactions = [
  { id: 1, name: 'Sarah Johnson', type: 'P2P Transfer', amount: -50, currency: 'USD', time: '2h ago' },
  { id: 2, name: 'Campus Cafeteria', type: 'Campus Payment', amount: -15.50, currency: 'USD', time: '5h ago' },
  { id: 3, name: 'Michael Chen', type: 'P2P Received', amount: 100, currency: 'EUR', time: '1d ago' }
];

export function Home({ onNavigate }: HomeProps) {
  const [isBalanceHidden, setIsBalanceHidden] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [totalBalanceUSD, setTotalBalanceUSD] = useState(0);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([]);
  const [isLoadingBalance, setIsLoadingBalance] = useState(true);
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(true);
  const [kycStatus, setKycStatus] = useState<'NOT_STARTED' | 'PENDING' | 'VERIFIED'>('NOT_STARTED');
  const [balancesByCurrency, setBalancesByCurrency] = useState<{ [key: string]: number }>({});
  
  const { theme, toggleTheme, logout, userName } = useAppContext();
  const displayName = userName || 'Guest';
  const initials = displayName.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2);

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
      INR: '₹'
    };
    return symbols[currency] || currency;
  };

  // Get KYC status from localStorage on component mount
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      setKycStatus(user.kycStatus || 'NOT_STARTED');
    }
  }, []);

  // Extracted fetch function so it can be called on-demand (e.g., after deposit)
  const fetchUserData = async () => {
    try {
      setIsLoadingBalance(true);
      setIsLoadingTransactions(true);

      const userStr = localStorage.getItem('user');
      if (!userStr) {
        console.log('No user found in localStorage');
        return;
      }

      const user = JSON.parse(userStr);

      // Fetch user wallets
      const userWallets = await walletApi.getUserWallets(user.id);
      setWallets(userWallets);

      // Calculate balances by currency
      const balances: { [key: string]: number } = {};
      userWallets.forEach(wallet => {
        const currency = wallet.currencyCode || 'USD';
        balances[currency] = (balances[currency] || 0) + wallet.balance;
      });
      setBalancesByCurrency(balances);
      setIsLoadingBalance(false);

      // Fetch recent transactions
      const transactions = await transactionApi.getUserTransactions(user.id);

      // Handle paginated response - extract the transactions array
      let transactionsArray = [];
      if (Array.isArray(transactions)) {
        transactionsArray = transactions;
      } else if (transactions && typeof transactions === 'object') {
        if ('transactions' in transactions) {
          transactionsArray = (transactions as any).transactions || [];
        } else if ('content' in transactions) {
          transactionsArray = (transactions as any).content || [];
        }
      }

      setRecentTransactions(transactionsArray.slice(0, 5));
    } catch (error: any) {
      console.error('Failed to fetch user data:', error);
      toast.error('Failed to fetch latest data');
      setIsLoadingBalance(false);
    } finally {
      setIsLoadingTransactions(false);
    }
  };

  // Fetch user data and balances from API
  useEffect(() => {
    fetchUserData();
  }, []);

  const bgColor = theme === 'dark' ? 'bg-gray-900' : 'bg-gray-50';
  const cardBg = theme === 'dark' ? 'bg-gray-800' : 'bg-white';
  const textColor = theme === 'dark' ? 'text-gray-100' : 'text-gray-900';
  const textSecondary = theme === 'dark' ? 'text-gray-400' : 'text-gray-600';

  // Helper functions

  const getCurrencyColor = (currency: string) => {
    const colors: { [key: string]: string } = {
      'USD': '#2ECC71',
      'EUR': '#9B59B6',
      'GBP': '#E67E22',
      'JPY': '#E74C3C',
      'INR': '#F4C542'
    };
    return colors[currency] || '#3498DB';
  };

  const getDescription = (tx: Transaction) => {
    switch (tx.type) {
      case 'P2P_TRANSFER':
        // Parse the description to extract recipient/sender info
        const desc = tx.description || '';
        if (desc.includes('to ')) {
          // This is an outgoing transfer: "P2P transfer to X"
          const recipient = desc.split('to ')[1];
          return `You sent ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)} to ${recipient}`;
        } else if (desc.includes('from ')) {
          // This is an incoming transfer: "P2P transfer from X"
          const sender = desc.split('from ')[1];
          return `${sender} sent you ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
        } else {
          // Fallback for generic descriptions
          return `P2P Transfer ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
        }
      case 'CAMPUS_PAYMENT':
        return `Campus Payment ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
      case 'REMITTANCE':
        return `Remittance ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
      case 'ADD_FUNDS':
        return `Added ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
      case 'DEDUCT_FUNDS':
        return `Deducted ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
      default:
        return `${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
    }
  };

  const formatTransactionType = (type: string) => {
    const types: { [key: string]: string } = {
      'P2P_TRANSFER': 'P2P Transfer',
      'CAMPUS_PAYMENT': 'Campus Payment',
      'REMITTANCE': 'Remittance',
      'ADD_FUNDS': 'Credit',
      'DEDUCT_FUNDS': 'Debit'
    };
    return types[type] || type;
  };

  const isCreditTransaction = (tx: Transaction) => {
    return tx.type === 'ADD_FUNDS';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);
    
    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${Math.floor(diffInHours)}h ago`;
    if (diffInHours < 48) return '1d ago';
    return `${Math.floor(diffInHours / 24)}d ago`;
  };

  // Get current user ID from localStorage
  const getUserId = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      return user.id;
    }
    return null;
  };

  return (
    <div className={`min-h-screen ${bgColor}`}>
      {/* Header */}
      <div className="bg-gradient-to-br from-indigo-600 to-purple-600 rounded-b-3xl p-6 pb-8">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <Avatar className="w-12 h-12 border-2 border-white">
              <AvatarFallback className="bg-white text-indigo-600">{initials}</AvatarFallback>
            </Avatar>
            <div>
              <p className="text-white/80 text-sm">Welcome back,</p>
              <p className="text-white">{displayName}</p>
            </div>
          </div>
          <div className="flex gap-2">
            <button 
              onClick={() => setShowNotifications(true)}
              className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center relative"
            >
              <Bell className="text-white" size={20} />
              <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </button>
            <button 
              onClick={() => setShowSettings(true)}
              className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center"
            >
              <Settings className="text-white" size={20} />
            </button>
          </div>
        </div>

        {/* Currency Balances */}
        <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-4">
          <div className="flex items-center justify-between mb-2">
            <p className="text-white/80 text-sm">My Balances</p>
            <button 
              onClick={() => setIsBalanceHidden(!isBalanceHidden)}
              className="text-white/80"
            >
              {isBalanceHidden ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
          {isBalanceHidden ? (
            <p className="text-white text-3xl">••••••</p>
          ) : isLoadingBalance ? (
            <p className="text-white text-3xl">Loading...</p>
          ) : (
            <div className="space-y-2">
              {Object.entries(balancesByCurrency).map(([currency, balance]) => (
                <div key={currency} className="flex justify-between items-center">
                  <span className="text-white text-lg">{currency}</span>
                  <span className="text-white text-lg font-semibold">
                    {getCurrencySymbol(currency)}{balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="px-6 -mt-6 mb-6">
        <Card className={`p-4 shadow-lg border-0 ${cardBg}`}>
          <p className={`text-sm mb-3 ${textColor}`}>Quick Actions</p>
          <div className="grid grid-cols-2 gap-4">
            <button 
              onClick={() => onNavigate('p2p')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-cyan-400 to-cyan-600 flex items-center justify-center">
                <Send className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Send</span>
            </button>
            <button 
              onClick={() => onNavigate('conversion')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-slate-400 to-slate-600 flex items-center justify-center">
                <ArrowLeftRight className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Convert</span>
            </button>
            <button 
              onClick={() => onNavigate('remittance')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center">
                <Send className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Remittance</span>
            </button>
            <button 
              onClick={() => onNavigate('alerts')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center">
                <AlertCircleIcon className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Alerts</span>
            </button>
          </div>
        </Card>
      </div>

      {/* Wallet Cards */}
      <div className="px-6 mb-6">
        <div className="flex items-center justify-between mb-3">
          <p className={textColor}>My Wallets</p>
          <Button variant="ghost" size="sm" className="text-sm h-auto p-0">
            View All
          </Button>
        </div>
        <div className="space-y-3">
          {wallets.length > 0 ? (
            wallets.slice(0, wallets.length).map((wallet) => (
              <WalletCard 
                key={wallet.id}
                wallet={wallet}
                isBalanceHidden={isBalanceHidden}
                onDelete={() => {
                  setWallets(prev => prev.filter(w => w.id !== wallet.id));
                  toast.success('Wallet deleted successfully');
                }}
                  onNavigate={onNavigate}
                  onWalletUpdated={fetchUserData}
              />
            ))
          ) : (
            <div className={`text-center py-8 ${textSecondary}`}>
              <p>No wallets found</p>
            </div>
          )}
          
          {/* Create Wallet Button - Always visible */}
          <div className="mt-4">
            <Button 
              onClick={() => onNavigate('createWallet')}
              className="w-full"
              variant="outline"
            >
              <Plus className="w-4 h-4 mr-2" />
              Create New Wallet
            </Button>
          </div>
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="px-6 pb-6">
        <div className="flex items-center justify-between mb-3">
          <p className={textColor}>Recent Activity</p>
          <Button 
            variant="ghost" 
            size="sm" 
            className="text-sm h-auto p-0"
          >
            View All
          </Button>
        </div>
        <Card className={`divide-y border-0 shadow-lg ${cardBg}`}>
          {recentTransactions.length > 0 ? (
            recentTransactions.map((tx) => (
              <div key={tx.transactionId} className="p-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                    isCreditTransaction(tx) ? 'bg-green-100 dark:bg-green-900' : 'bg-red-100 dark:bg-red-900'
                  }`}>
                    <Clock className={isCreditTransaction(tx) ? 'text-green-600' : 'text-red-600'} size={18} />
                  </div>
                  <div>
                    <p className={`text-sm ${textColor}`}>{getDescription(tx)}</p>
                    <p className={`text-xs ${textSecondary}`}>{formatTransactionType(tx.type)}</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={`text-sm ${isCreditTransaction(tx) ? 'text-green-600' : 'text-red-600'}`}>
                    {isCreditTransaction(tx) ? '+' : '-'}{getCurrencySymbol(tx.currencyCode)}{Math.abs(tx.amount).toFixed(2)}
                  </p>
                  <p className={`text-xs ${textSecondary}`}>{formatDate(tx.createdAt)}</p>
                </div>
              </div>
            ))
          ) : isLoadingTransactions ? (
            <div className="text-center py-8">
              <p className={textSecondary}>Loading transactions...</p>
            </div>
          ) : (
            <div className={`text-center py-8 ${textSecondary}`}>
              <p>No recent transactions</p>
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

      {/* Settings Dialog */}
      <Dialog open={showSettings} onOpenChange={setShowSettings}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Settings</DialogTitle>
            <DialogDescription>
              Customize your app experience
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label>Dark Mode</Label>
                <p className="text-xs text-gray-500">Toggle between light and dark theme</p>
              </div>
              <Switch 
                checked={theme === 'dark'} 
                onCheckedChange={toggleTheme}
              />
            </div>
            
            <div className="border-t pt-4">
              <Button 
                variant="outline" 
                className="w-full flex items-center gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
                onClick={() => {
                  logout();
                  setShowSettings(false);
                }}
              >
                <LogOut size={16} />
                Logout
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

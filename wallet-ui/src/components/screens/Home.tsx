import React, { useState } from 'react';
import { WalletCard } from '../wallet/WalletCard';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { 
  Eye, 
  EyeOff, 
  Send, 
  QrCode, 
  ArrowLeftRight, 
  Bell, 
  Settings,
  TrendingUp,
  Clock,
  AlertCircle,
  Send as SendIcon
} from 'lucide-react';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Badge } from '../ui/badge';
import { useAppContext } from '../../App';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { toast } from 'sonner@2.0.3';
import { Switch } from '../ui/switch';
import { Label } from '../ui/label';

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

const notifications = [
  { id: 1, message: 'P2P transfer received: $100 from Sarah Johnson', time: '2h ago' },
  { id: 2, message: 'Your EUR wallet balance is low', time: '5h ago' },
  { id: 3, message: 'New rate alert triggered for JPY', time: '1d ago' }
];

export function Home({ onNavigate }: HomeProps) {
  const [isBalanceHidden, setIsBalanceHidden] = useState(false);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const { userName, theme, toggleTheme } = useAppContext();
  
  const totalBalanceUSD = 8250.40;
  const displayName = userName || 'Guest';
  const initials = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

  const bgColor = theme === 'dark' ? 'bg-gray-900' : 'bg-gray-50';
  const cardBg = theme === 'dark' ? 'bg-gray-800' : 'bg-white';
  const textColor = theme === 'dark' ? 'text-gray-100' : 'text-gray-900';
  const textSecondary = theme === 'dark' ? 'text-gray-400' : 'text-gray-600';

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

        {/* Total Balance */}
        <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-4">
          <div className="flex items-center justify-between mb-2">
            <p className="text-white/80 text-sm">Total Balance (USD)</p>
            <button 
              onClick={() => setIsBalanceHidden(!isBalanceHidden)}
              className="text-white/80"
            >
              {isBalanceHidden ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
          {isBalanceHidden ? (
            <p className="text-white text-3xl">••••••</p>
          ) : (
            <p className="text-white text-3xl">${totalBalanceUSD.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
          )}
          <div className="flex items-center gap-2 mt-2">
            <TrendingUp size={14} className="text-green-300" />
            <p className="text-green-300 text-xs">+5.2% this month</p>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="px-6 -mt-6 mb-6">
        <Card className={`p-4 shadow-lg border-0 ${cardBg}`}>
          <p className={`text-sm mb-3 ${textColor}`}>Quick Actions</p>
          <div className="grid grid-cols-3 gap-3">
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
              onClick={() => onNavigate('campus')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-teal-400 to-teal-600 flex items-center justify-center">
                <QrCode className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Pay</span>
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
              onClick={() => onNavigate('analytics')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center">
                <TrendingUp className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Analytics</span>
            </button>
            <button 
              onClick={() => onNavigate('remittance')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-purple-400 to-purple-600 flex items-center justify-center">
                <SendIcon className="text-white" size={20} />
              </div>
              <span className={`text-xs ${textColor}`}>Remittance</span>
            </button>
            <button 
              onClick={() => onNavigate('alerts')}
              className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center">
                <AlertCircle className="text-white" size={20} />
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
          {currencies.slice(0, 3).map((wallet) => (
            <WalletCard 
              key={wallet.currency}
              {...wallet}
              isBalanceHidden={isBalanceHidden}
            />
          ))}
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
            onClick={() => onNavigate('analytics')}
          >
            View All
          </Button>
        </div>
        <Card className={`divide-y border-0 shadow-lg ${cardBg}`}>
          {recentTransactions.map((tx) => (
            <div key={tx.id} className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                  tx.type.includes('Received') ? 'bg-green-100 dark:bg-green-900' : 'bg-gray-100 dark:bg-gray-700'
                }`}>
                  <Clock className={tx.type.includes('Received') ? 'text-green-600' : 'text-gray-600'} size={18} />
                </div>
                <div>
                  <p className={`text-sm ${textColor}`}>{tx.name}</p>
                  <p className={`text-xs ${textSecondary}`}>{tx.type}</p>
                </div>
              </div>
              <div className="text-right">
                <p className={`text-sm ${tx.amount > 0 ? 'text-green-600' : textColor}`}>
                  {tx.amount > 0 ? '+' : ''}{tx.amount} {tx.currency}
                </p>
                <p className={`text-xs ${textSecondary}`}>{tx.time}</p>
              </div>
            </div>
          ))}
        </Card>
      </div>

      {/* Notifications Dialog */}
      <Dialog open={showNotifications} onOpenChange={setShowNotifications}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Notifications</DialogTitle>
            <DialogDescription>
              Stay updated with your recent activities
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3 py-4">
            {notifications.map((notif) => (
              <div key={notif.id} className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p className="text-sm text-gray-900 dark:text-gray-100">{notif.message}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{notif.time}</p>
              </div>
            ))}
          </div>
        </DialogContent>
      </Dialog>

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
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

import React, { useState } from 'react';
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
  Zap,
  ChevronRight,
  Sparkles
} from 'lucide-react';
import { Avatar, AvatarFallback } from '../ui/avatar';

interface HomeProps {
  onNavigate: (screen: string) => void;
}

const currencies = [
  { currency: 'USD', symbol: '$', balance: 2450.75, color: '#2ECC71', bgColor: 'from-emerald-500/20 to-teal-500/20', borderColor: 'border-emerald-500/30' },
  { currency: 'EUR', symbol: '€', balance: 1820.50, color: '#9B59B6', bgColor: 'from-purple-500/20 to-pink-500/20', borderColor: 'border-purple-500/30' },
  { currency: 'GBP', symbol: '£', balance: 980.25, color: '#E67E22', bgColor: 'from-orange-500/20 to-amber-500/20', borderColor: 'border-orange-500/30' },
  { currency: 'JPY', symbol: '¥', balance: 125000, color: '#E74C3C', bgColor: 'from-red-500/20 to-rose-500/20', borderColor: 'border-red-500/30' },
  { currency: 'INR', symbol: '₹', balance: 45500, color: '#F4C542', bgColor: 'from-amber-500/20 to-yellow-500/20', borderColor: 'border-amber-500/30' }
];

const quickActions = [
  { id: 'p2p', label: 'Send', icon: Send, gradient: 'from-cyan-500 to-blue-500', screen: 'p2p' },
  { id: 'campus', label: 'Pay', icon: QrCode, gradient: 'from-teal-500 to-emerald-500', screen: 'campus' },
  { id: 'conversion', label: 'Convert', icon: ArrowLeftRight, gradient: 'from-purple-500 to-pink-500', screen: 'conversion' },
  { id: 'analytics', label: 'Stats', icon: TrendingUp, gradient: 'from-indigo-500 to-purple-500', screen: 'analytics' },
];

const recentTransactions = [
  { id: 1, name: 'Sarah Johnson', type: 'received', amount: 50, currency: 'USD', time: '2h', emoji: '👤' },
  { id: 2, name: 'Campus Cafe', type: 'sent', amount: -15.50, currency: 'USD', time: '5h', emoji: '☕' },
  { id: 3, name: 'Bookstore', type: 'sent', amount: -89, currency: 'USD', time: '1d', emoji: '📚' },
];

export function Home({ onNavigate }: HomeProps) {
  const [isBalanceHidden, setIsBalanceHidden] = useState(false);
  const totalBalanceUSD = 8250.40;

  return (
    <div className="min-h-screen bg-black">
      {/* Unique Header with Diagonal Cut */}
      <div className="relative pb-24">
        <div className="absolute inset-0 bg-gradient-to-br from-zinc-900 via-zinc-800 to-black" style={{
          clipPath: 'polygon(0 0, 100% 0, 100% calc(100% - 60px), 0 100%)'
        }} />
        
        {/* Decorative Elements */}
        <div className="absolute top-10 right-10 w-32 h-32 rounded-full bg-gradient-to-br from-indigo-500/20 to-purple-500/20 blur-2xl" />
        
        <div className="relative z-10 px-6 pt-12">
          {/* Top Bar */}
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <Avatar className="w-12 h-12 border-2 border-white/10">
                <AvatarFallback className="bg-gradient-to-br from-indigo-500 to-purple-500 text-white">JD</AvatarFallback>
              </Avatar>
              <div>
                <p className="text-xs text-gray-500">Welcome back</p>
                <p className="text-white">John Doe</p>
              </div>
            </div>
            <div className="flex gap-2">
              <button className="w-10 h-10 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors relative">
                <Bell size={18} className="text-white" />
                <span className="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full" />
              </button>
              <button 
                onClick={() => onNavigate('settings')}
                className="w-10 h-10 rounded-xl bg-white/5 backdrop-blur-sm border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors"
              >
                <Settings size={18} className="text-white" />
              </button>
            </div>
          </div>

          {/* Balance Card with Asymmetric Design */}
          <div className="relative">
            <div className="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-3xl blur-xl opacity-30" />
            <div className="relative bg-gradient-to-br from-zinc-800/80 to-zinc-900/80 backdrop-blur-xl border border-white/10 rounded-3xl p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <p className="text-xs text-gray-400 mb-1 flex items-center gap-2">
                    Total Portfolio
                    <Sparkles size={12} className="text-yellow-500" />
                  </p>
                  {isBalanceHidden ? (
                    <p className="text-4xl text-white">••••••</p>
                  ) : (
                    <p className="text-4xl text-white tracking-tight">${totalBalanceUSD.toLocaleString('en-US', { minimumFractionDigits: 2 })}</p>
                  )}
                </div>
                <button 
                  onClick={() => setIsBalanceHidden(!isBalanceHidden)}
                  className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors"
                >
                  {isBalanceHidden ? <EyeOff size={18} className="text-gray-400" /> : <Eye size={18} className="text-gray-400" />}
                </button>
              </div>
              
              <div className="flex items-center gap-2 mb-4">
                <div className="flex items-center gap-1 px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-500/30">
                  <TrendingUp size={12} className="text-emerald-400" />
                  <span className="text-xs text-emerald-400">+5.2%</span>
                </div>
                <p className="text-xs text-gray-500">vs last month</p>
              </div>

              {/* Quick Stats */}
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                  <p className="text-xs text-gray-500 mb-1">Received</p>
                  <p className="text-white">$1,240</p>
                </div>
                <div className="bg-white/5 rounded-xl p-3 border border-white/5">
                  <p className="text-xs text-gray-500 mb-1">Spent</p>
                  <p className="text-white">$890</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions - Unique Circular Layout */}
      <div className="px-6 -mt-16 mb-8">
        <div className="grid grid-cols-4 gap-3">
          {quickActions.map((action) => {
            const Icon = action.icon;
            return (
              <button
                key={action.id}
                onClick={() => onNavigate(action.screen)}
                className="flex flex-col items-center gap-2 group"
              >
                <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${action.gradient} flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform relative`}>
                  <Icon className="text-white" size={24} />
                  <div className="absolute inset-0 rounded-2xl bg-white/0 group-hover:bg-white/10 transition-colors" />
                </div>
                <span className="text-xs text-gray-400 group-hover:text-white transition-colors">{action.label}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* Currency Cards - Horizontal Scroll with Unique Card Design */}
      <div className="px-6 mb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-white text-lg">Your Wallets</h2>
          <button className="text-xs text-gray-400 hover:text-white transition-colors flex items-center gap-1">
            View All
            <ChevronRight size={14} />
          </button>
        </div>
        
        <div className="space-y-3">
          {currencies.slice(0, 3).map((wallet, index) => (
            <div
              key={wallet.currency}
              className="relative group cursor-pointer"
              style={{ 
                transform: `rotate(${index === 1 ? '0deg' : index === 0 ? '-0.5deg' : '0.5deg'})`,
                transition: 'transform 0.3s'
              }}
            >
              <div className={`absolute inset-0 bg-gradient-to-r ${wallet.bgColor} rounded-2xl blur-lg opacity-0 group-hover:opacity-100 transition-opacity`} />
              <div className={`relative bg-zinc-900/80 backdrop-blur-xl border ${wallet.borderColor} rounded-2xl p-5`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-white/10 to-white/5 border border-white/10 flex items-center justify-center">
                      <span className="text-xl">{wallet.symbol}</span>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">{wallet.currency}</p>
                      <p className="text-white text-xl">
                        {isBalanceHidden ? '••••••' : `${wallet.symbol}${wallet.balance.toLocaleString()}`}
                      </p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button className="w-9 h-9 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors">
                      <Send size={14} className="text-gray-400" />
                    </button>
                    <button className="w-9 h-9 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors">
                      <Zap size={14} className="text-gray-400" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Activity - Timeline Style */}
      <div className="px-6 pb-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-white text-lg">Recent Activity</h2>
          <button 
            onClick={() => onNavigate('analytics')}
            className="text-xs text-gray-400 hover:text-white transition-colors flex items-center gap-1"
          >
            See All
            <ChevronRight size={14} />
          </button>
        </div>

        <div className="space-y-3">
          {recentTransactions.map((tx, index) => (
            <div key={tx.id} className="relative">
              {index < recentTransactions.length - 1 && (
                <div className="absolute left-6 top-12 bottom-0 w-px bg-gradient-to-b from-zinc-800 to-transparent" />
              )}
              <div className="bg-zinc-900/50 backdrop-blur-xl border border-zinc-800 rounded-2xl p-4 hover:border-zinc-700 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-xl bg-zinc-800 flex items-center justify-center flex-shrink-0 text-xl">
                    {tx.emoji}
                  </div>
                  <div className="flex-1">
                    <p className="text-white text-sm">{tx.name}</p>
                    <p className="text-xs text-gray-500">{tx.time} ago</p>
                  </div>
                  <div className="text-right">
                    <p className={`${tx.amount > 0 ? 'text-emerald-400' : 'text-white'}`}>
                      {tx.amount > 0 ? '+' : ''}{tx.amount} {tx.currency}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

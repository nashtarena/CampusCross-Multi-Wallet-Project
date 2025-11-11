import React from 'react';
import { Card } from '../ui/card';
import { ArrowUpRight, ArrowDownLeft, TrendingUp } from 'lucide-react';

interface WalletCardProps {
  currency: string;
  symbol: string;
  balance: number;
  color: string;
  isBalanceHidden?: boolean;
}

export function WalletCard({ currency, symbol, balance, color, isBalanceHidden = false }: WalletCardProps) {
  return (
    <div className="relative group">
      {/* Glow Effect */}
      <div 
        className="absolute -inset-1 rounded-3xl blur-xl opacity-0 group-hover:opacity-30 transition-opacity"
        style={{ background: color }}
      />
      
      {/* Card */}
      <Card 
        className="relative overflow-hidden border-0 bg-zinc-900/80 backdrop-blur-xl"
        style={{ 
          borderLeft: `3px solid ${color}`,
          borderRadius: '24px'
        }}
      >
        <div className="p-6">
          {/* Header */}
          <div className="flex justify-between items-start mb-6">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <div 
                  className="w-3 h-3 rounded-full animate-pulse"
                  style={{ backgroundColor: color }}
                />
                <p className="text-xs text-gray-500 uppercase tracking-wider">{currency}</p>
              </div>
              <div>
                {isBalanceHidden ? (
                  <p className="text-3xl text-white">••••••</p>
                ) : (
                  <div className="flex items-baseline gap-1">
                    <span className="text-2xl text-gray-400">{symbol}</span>
                    <span className="text-3xl text-white tracking-tight">
                      {balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  </div>
                )}
              </div>
            </div>
            
            {/* Performance Badge */}
            <div className="px-2 py-1 rounded-lg bg-emerald-500/20 border border-emerald-500/30 flex items-center gap-1">
              <TrendingUp size={12} className="text-emerald-400" />
              <span className="text-xs text-emerald-400">+2.3%</span>
            </div>
          </div>
          
          {/* Action Buttons */}
          <div className="flex gap-2">
            <button 
              className="flex-1 h-10 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 transition-colors flex items-center justify-center gap-2 group/btn"
              style={{ borderColor: `${color}20` }}
            >
              <ArrowUpRight size={16} className="text-gray-400 group-hover/btn:text-white transition-colors" />
              <span className="text-sm text-gray-400 group-hover/btn:text-white transition-colors">Send</span>
            </button>
            <button 
              className="flex-1 h-10 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 transition-colors flex items-center justify-center gap-2 group/btn"
              style={{ borderColor: `${color}20` }}
            >
              <ArrowDownLeft size={16} className="text-gray-400 group-hover/btn:text-white transition-colors" />
              <span className="text-sm text-gray-400 group-hover/btn:text-white transition-colors">Receive</span>
            </button>
          </div>
          
          {/* Decorative Corner Elements */}
          <div 
            className="absolute -right-4 -top-4 w-24 h-24 rounded-full opacity-10"
            style={{ background: `radial-gradient(circle, ${color} 0%, transparent 70%)` }}
          />
          <div 
            className="absolute -left-4 -bottom-4 w-16 h-16 rounded-full opacity-5"
            style={{ background: `radial-gradient(circle, ${color} 0%, transparent 70%)` }}
          />
        </div>
      </Card>
    </div>
  );
}

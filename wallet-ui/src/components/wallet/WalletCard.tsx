import React from 'react';
import { Card } from '../ui/card';
import { ArrowUpRight, ArrowDownLeft, Eye, EyeOff } from 'lucide-react';

interface WalletCardProps {
  currency: string;
  symbol: string;
  balance: number;
  color: string;
  isBalanceHidden?: boolean;
}

export function WalletCard({ currency, symbol, balance, color, isBalanceHidden = false }: WalletCardProps) {
  return (
    <Card 
      className="relative overflow-hidden border-0 shadow-lg"
      style={{ background: `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)` }}
    >
      <div className="p-6 text-white">
        <div className="flex justify-between items-start mb-6">
          <div>
            <p className="text-white/80 text-sm">{currency} Wallet</p>
            <div className="mt-2">
              {isBalanceHidden ? (
                <p className="text-2xl">••••••</p>
              ) : (
                <p className="text-3xl">{symbol}{balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
              )}
            </div>
          </div>
          <div className="bg-white/20 backdrop-blur-sm rounded-lg px-3 py-1">
            <p className="text-xs">{currency}</p>
          </div>
        </div>
        
        <div className="flex gap-2">
          <button className="flex-1 bg-white/20 backdrop-blur-sm hover:bg-white/30 transition-colors rounded-lg py-2 px-3 flex items-center justify-center gap-2">
            <ArrowUpRight size={16} />
            <span className="text-sm">Send</span>
          </button>
          <button className="flex-1 bg-white/20 backdrop-blur-sm hover:bg-white/30 transition-colors rounded-lg py-2 px-3 flex items-center justify-center gap-2">
            <ArrowDownLeft size={16} />
            <span className="text-sm">Receive</span>
          </button>
        </div>
        
        {/* Decorative pattern */}
        <div className="absolute -right-8 -bottom-8 w-32 h-32 rounded-full bg-white/5" />
        <div className="absolute -right-4 -top-4 w-20 h-20 rounded-full bg-white/5" />
      </div>
    </Card>
  );
}

import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ArrowLeft, Send, Search, Zap, CheckCircle2 } from 'lucide-react';
import { Avatar, AvatarFallback } from '../ui/avatar';

interface P2PTransferProps {
  onBack: () => void;
}

const recentContacts = [
  { id: 1, name: 'Sarah Johnson', studentId: 'STU123456', avatar: 'SJ', color: 'from-pink-500 to-rose-500' },
  { id: 2, name: 'Michael Chen', studentId: 'STU789012', avatar: 'MC', color: 'from-blue-500 to-cyan-500' },
  { id: 3, name: 'Emma Davis', studentId: 'STU345678', avatar: 'ED', color: 'from-purple-500 to-indigo-500' },
  { id: 4, name: 'Alex Kumar', studentId: 'STU901234', avatar: 'AK', color: 'from-emerald-500 to-teal-500' }
];

const transactionHistory = [
  { id: 1, name: 'Sarah Johnson', amount: 50, currency: 'USD', date: 'Today', status: 'completed', type: 'sent' },
  { id: 2, name: 'Michael Chen', amount: 75, currency: 'EUR', date: 'Yesterday', status: 'completed', type: 'received' },
  { id: 3, name: 'Emma Davis', amount: 100, currency: 'GBP', date: '2 days ago', status: 'pending', type: 'sent' }
];

export function P2PTransfer({ onBack }: P2PTransferProps) {
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [amount, setAmount] = useState('');
  const [recipient, setRecipient] = useState('');
  const [activeTab, setActiveTab] = useState('send');

  return (
    <div className="min-h-screen bg-black">
      {/* Header with Gradient Accent */}
      <div className="relative pb-8">
        <div className="absolute inset-0 bg-gradient-to-br from-cyan-900/30 via-blue-900/20 to-black" style={{
          clipPath: 'polygon(0 0, 100% 0, 100% calc(100% - 50px), 0 100%)'
        }} />
        
        <div className="absolute top-20 right-0 w-40 h-40 rounded-full bg-gradient-to-br from-cyan-500/20 to-blue-500/20 blur-3xl" />
        
        <div className="relative z-10 p-6 pt-12">
          <div className="flex items-center gap-3 mb-6">
            <button 
              onClick={onBack}
              className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors"
            >
              <ArrowLeft size={20} className="text-white" />
            </button>
            <div>
              <h1 className="text-2xl text-white">P2P Transfer</h1>
              <p className="text-sm text-gray-400">Send money instantly</p>
            </div>
          </div>

          {/* Custom Tab Pills */}
          <div className="flex gap-2 bg-zinc-900/50 backdrop-blur-xl border border-zinc-800 rounded-2xl p-1.5">
            <button
              onClick={() => setActiveTab('send')}
              className={`flex-1 py-3 rounded-xl transition-all ${
                activeTab === 'send'
                  ? 'bg-gradient-to-r from-cyan-500 to-blue-500 text-white shadow-lg shadow-cyan-500/30'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              Send
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`flex-1 py-3 rounded-xl transition-all ${
                activeTab === 'history'
                  ? 'bg-gradient-to-r from-cyan-500 to-blue-500 text-white shadow-lg shadow-cyan-500/30'
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              History
            </button>
          </div>
        </div>
      </div>

      <div className="px-6 -mt-4">
        {activeTab === 'send' ? (
          <div className="space-y-6">
            {/* Send Form with Unique Design */}
            <div className="bg-zinc-900/50 backdrop-blur-xl border border-zinc-800 rounded-3xl p-6">
              <div className="space-y-5">
                {/* Recipient Search */}
                <div className="space-y-2">
                  <Label className="text-gray-400 text-sm">Send To</Label>
                  <div className="relative">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" size={20} />
                    <Input
                      placeholder="Student ID or Mobile"
                      className="pl-12 h-14 rounded-2xl bg-black/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
                      value={recipient}
                      onChange={(e) => setRecipient(e.target.value)}
                    />
                  </div>
                </div>

                {/* Amount Input with Currency Selector */}
                <div className="space-y-2">
                  <Label className="text-gray-400 text-sm">Amount</Label>
                  <div className="flex gap-3">
                    <Select value={selectedCurrency} onValueChange={setSelectedCurrency}>
                      <SelectTrigger className="w-28 h-14 rounded-2xl bg-black/50 border-zinc-800 text-white">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="USD">USD $</SelectItem>
                        <SelectItem value="EUR">EUR €</SelectItem>
                        <SelectItem value="GBP">GBP £</SelectItem>
                        <SelectItem value="JPY">JPY ¥</SelectItem>
                        <SelectItem value="INR">INR ₹</SelectItem>
                      </SelectContent>
                    </Select>
                    <Input
                      type="number"
                      placeholder="0.00"
                      className="flex-1 h-14 rounded-2xl bg-black/50 border-zinc-800 text-white text-2xl placeholder:text-gray-600 focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                    />
                  </div>
                </div>

                {/* Note */}
                <div className="space-y-2">
                  <Label className="text-gray-400 text-sm">Note (Optional)</Label>
                  <Input
                    placeholder="What's this for?"
                    className="h-14 rounded-2xl bg-black/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
                  />
                </div>

                {/* Fee Info */}
                <div className="flex items-center justify-between p-4 bg-cyan-500/10 border border-cyan-500/20 rounded-2xl">
                  <div className="flex items-center gap-2">
                    <Zap size={16} className="text-cyan-400" />
                    <span className="text-sm text-gray-300">Transfer Fee</span>
                  </div>
                  <span className="text-cyan-400">Free</span>
                </div>

                {/* Send Button */}
                <Button 
                  className="w-full h-14 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600 text-white shadow-xl shadow-cyan-500/30"
                >
                  <Send size={20} className="mr-2" />
                  Send Money
                </Button>
              </div>
            </div>

            {/* Quick Contacts - Horizontal Scroll */}
            <div>
              <h3 className="text-white mb-4 flex items-center gap-2">
                <Zap size={18} className="text-cyan-400" />
                Quick Send
              </h3>
              <div className="flex gap-3 overflow-x-auto pb-2 -mx-6 px-6 scrollbar-hide">
                {recentContacts.map((contact) => (
                  <button
                    key={contact.id}
                    onClick={() => setRecipient(contact.studentId)}
                    className="flex flex-col items-center gap-2 group flex-shrink-0"
                  >
                    <div className="relative">
                      <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${contact.color} flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform`}>
                        <span className="text-white text-lg">{contact.avatar}</span>
                      </div>
                      <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-lg bg-black border-2 border-emerald-500 flex items-center justify-center">
                        <div className="w-2 h-2 rounded-full bg-emerald-500" />
                      </div>
                    </div>
                    <p className="text-xs text-gray-400 text-center max-w-[80px] truncate group-hover:text-white transition-colors">
                      {contact.name.split(' ')[0]}
                    </p>
                  </button>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-3 pb-6">
            {transactionHistory.map((tx) => (
              <div 
                key={tx.id}
                className="bg-zinc-900/50 backdrop-blur-xl border border-zinc-800 rounded-2xl p-4 hover:border-zinc-700 transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${
                      tx.status === 'completed' 
                        ? tx.type === 'received' 
                          ? 'bg-emerald-500/20' 
                          : 'bg-blue-500/20'
                        : 'bg-amber-500/20'
                    }`}>
                      {tx.status === 'completed' ? (
                        <CheckCircle2 
                          className={tx.type === 'received' ? 'text-emerald-400' : 'text-blue-400'} 
                          size={20} 
                        />
                      ) : (
                        <Zap className="text-amber-400" size={20} />
                      )}
                    </div>
                    <div>
                      <p className="text-white">{tx.name}</p>
                      <p className="text-xs text-gray-500">{tx.date}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className={`${
                      tx.type === 'received' ? 'text-emerald-400' : 'text-white'
                    }`}>
                      {tx.type === 'received' ? '+' : '-'}{tx.amount} {tx.currency}
                    </p>
                    <p className="text-xs text-gray-500 capitalize">{tx.status}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <style>{`
        .scrollbar-hide::-webkit-scrollbar {
          display: none;
        }
        .scrollbar-hide {
          -ms-overflow-style: none;
          scrollbar-width: none;
        }
      `}</style>
    </div>
  );
}

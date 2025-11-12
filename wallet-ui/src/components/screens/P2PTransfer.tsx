import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ArrowLeft, Send, Search, Clock, CheckCircle2 } from 'lucide-react';
import { Avatar, AvatarFallback } from '../ui/avatar';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { toast } from 'sonner';
import { useAppContext } from '../../App';

interface P2PTransferProps {
  onBack: () => void;
}

const recentContacts = [
  { id: 1, name: 'Sarah Johnson', studentId: 'STU123456', avatar: 'SJ' },
  { id: 2, name: 'Michael Chen', studentId: 'STU789012', avatar: 'MC' },
  { id: 3, name: 'Emma Davis', studentId: 'STU345678', avatar: 'ED' }
];

const transactionHistory = [
  { id: 1, name: 'Sarah Johnson', amount: 50, currency: 'USD', date: '2024-11-09', status: 'completed' },
  { id: 2, name: 'Michael Chen', amount: -75, currency: 'EUR', date: '2024-11-08', status: 'completed' },
  { id: 3, name: 'Emma Davis', amount: 100, currency: 'GBP', date: '2024-11-07', status: 'pending' }
];

export function P2PTransfer({ onBack }: P2PTransferProps) {
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [amount, setAmount] = useState('');
  const [recipient, setRecipient] = useState('');
  const [activeTab, setActiveTab] = useState('send');
  const [showConfirmation, setShowConfirmation] = useState(false);
  const { theme } = useAppContext();

  const handleSendMoney = () => {
    if (!recipient) {
      toast.error('Please enter a recipient');
      return;
    }
    if (!amount || parseFloat(amount) <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }
    setShowConfirmation(true);
  };

  const confirmTransfer = () => {
    toast.success(`Successfully sent ${amount} ${selectedCurrency} to ${recipient}`);
    setShowConfirmation(false);
    setAmount('');
    setRecipient('');
  };

  const bgColor = theme === 'dark' ? 'from-cyan-900 to-blue-900' : 'from-cyan-50 to-blue-50';
  const cardBg = theme === 'dark' ? 'bg-gray-800' : 'bg-white';
  const textColor = theme === 'dark' ? 'text-gray-100' : 'text-gray-900';
  const inputBg = theme === 'dark' ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-200';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-[#00BCD4] p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">P2P Transfer</h1>
            <p className="text-sm text-white/80">Send money to students</p>
          </div>
        </div>
      </div>

      <div className="px-6 -mt-4">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="w-full bg-white shadow-lg rounded-xl p-1 mb-6">
            <TabsTrigger value="send" className="flex-1 rounded-lg">Send</TabsTrigger>
            <TabsTrigger value="history" className="flex-1 rounded-lg">History</TabsTrigger>
          </TabsList>

          <TabsContent value="send" className="mt-0 space-y-6">
            {/* Send Form */}
            <Card className="p-6 bg-white shadow-lg border-0">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label className="text-gray-700">Recipient</Label>
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <Input
                      placeholder="Student ID or Mobile Number"
                      className="pl-10 h-12 rounded-xl border-gray-200"
                      value={recipient}
                      onChange={(e) => setRecipient(e.target.value)}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="text-gray-700">Currency</Label>
                    <Select value={selectedCurrency} onValueChange={setSelectedCurrency}>
                      <SelectTrigger className="h-12 rounded-xl border-gray-200">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="USD">USD</SelectItem>
                        <SelectItem value="EUR">EUR</SelectItem>
                        <SelectItem value="GBP">GBP</SelectItem>
                        <SelectItem value="JPY">JPY</SelectItem>
                        <SelectItem value="INR">INR</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-gray-700">Amount</Label>
                    <Input
                      type="number"
                      placeholder="0.00"
                      className="h-12 rounded-xl border-gray-200"
                      value={amount}
                      onChange={(e) => setAmount(e.target.value)}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Note (Optional)</Label>
                  <Input
                    placeholder="Add a message"
                    className="h-12 rounded-xl border-gray-200"
                  />
                </div>

                <div className="bg-cyan-50 rounded-xl p-4">
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-600">Transfer Fee</span>
                    <span className="text-gray-900">Free</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-900">Total</span>
                    <span className="text-gray-900">{amount || '0.00'} {selectedCurrency}</span>
                  </div>
                </div>

                <Button 
                  className="w-full h-12 rounded-xl"
                  style={{ background: '#00BCD4' }}
                  onClick={handleSendMoney}
                >
                  <Send size={20} className="mr-2" />
                  Send Money
                </Button>
              </div>
            </Card>

            {/* Recent Contacts */}
            <div>
              <h3 className="text-gray-900 mb-3">Recent Contacts</h3>
              <div className="space-y-3">
                {recentContacts.map((contact) => (
                  <Card 
                    key={contact.id}
                    className="p-4 border-0 shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                    onClick={() => setRecipient(contact.studentId)}
                  >
                    <div className="flex items-center gap-3">
                      <Avatar className="w-12 h-12 bg-gradient-to-br from-cyan-400 to-cyan-600">
                        <AvatarFallback className="bg-transparent text-white">
                          {contact.avatar}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1">
                        <p className="text-gray-900">{contact.name}</p>
                        <p className="text-xs text-gray-500">{contact.studentId}</p>
                      </div>
                      <Send className="text-gray-400" size={18} />
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          </TabsContent>

          <TabsContent value="history" className="mt-0">
            <div className="space-y-3">
              {transactionHistory.map((tx) => (
                <Card key={tx.id} className="p-4 border-0 shadow-md">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                        tx.status === 'completed' ? 'bg-green-100' : 'bg-amber-100'
                      }`}>
                        {tx.status === 'completed' ? (
                          <CheckCircle2 className="text-green-600" size={20} />
                        ) : (
                          <Clock className="text-amber-600" size={20} />
                        )}
                      </div>
                      <div>
                        <p className="text-gray-900">{tx.name}</p>
                        <p className="text-xs text-gray-500">{tx.date}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className={`${tx.amount > 0 ? 'text-green-600' : 'text-gray-900'}`}>
                        {tx.amount > 0 ? '+' : ''}{tx.amount} {tx.currency}
                      </p>
                      <p className="text-xs text-gray-500 capitalize">{tx.status}</p>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          </TabsContent>
        </Tabs>
      </div>

      {/* Confirmation Dialog */}
      <Dialog open={showConfirmation} onOpenChange={setShowConfirmation}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Confirm Transfer</DialogTitle>
            <DialogDescription>
              Are you sure you want to send {amount} {selectedCurrency} to {recipient}?
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-4">
            <Button
              variant="outline"
              onClick={() => setShowConfirmation(false)}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmTransfer}
            >
              Confirm
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
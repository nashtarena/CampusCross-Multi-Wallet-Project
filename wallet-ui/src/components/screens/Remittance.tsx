import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ArrowLeft, Building2, Clock, CheckCircle2, AlertCircle } from 'lucide-react';
import { Progress } from '../ui/progress';
import { useAppContext } from '../../App';

interface RemittanceProps {
  onBack: () => void;
}

const transactions = [
  { id: 1, recipient: 'John Doe', bank: 'Chase Bank', amount: 500, currency: 'USD', status: 'completed', date: '2024-11-08' },
  { id: 2, recipient: 'Sarah Smith', bank: 'HSBC', amount: 300, currency: 'GBP', status: 'processing', date: '2024-11-09' },
  { id: 3, recipient: 'Mike Johnson', bank: 'Bank of America', amount: 750, currency: 'USD', status: 'pending', date: '2024-11-09' }
];

export function Remittance({ onBack }: RemittanceProps) {
  const [activeTab, setActiveTab] = useState('send');
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [amount, setAmount] = useState('');
  const { theme } = useAppContext();

  const fee = parseFloat(amount || '0') * 0.01; // 1% fee
  const totalAmount = parseFloat(amount || '0') + fee;

  const bgColor = theme === 'dark' ? 'from-blue-900 to-indigo-900' : 'from-blue-50 to-indigo-50';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Remittance</h1>
            <p className="text-sm text-white/80">International bank transfers</p>
          </div>
        </div>
      </div>

      <div className="px-6 -mt-4">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="w-full bg-white shadow-lg rounded-xl p-1 mb-6">
            <TabsTrigger value="send" className="flex-1 rounded-lg">Send</TabsTrigger>
            <TabsTrigger value="status" className="flex-1 rounded-lg">Status</TabsTrigger>
          </TabsList>

          <TabsContent value="send" className="mt-0 space-y-6">
            {/* Send Form */}
            <Card className="p-6 bg-white shadow-lg border-0">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label className="text-gray-700">Recipient Name</Label>
                  <Input
                    placeholder="Full name as per bank account"
                    className="h-12 rounded-xl border-gray-200"
                  />
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Bank Name</Label>
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <Input
                      placeholder="Enter bank name"
                      className="pl-10 h-12 rounded-xl border-gray-200"
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Account Number</Label>
                  <Input
                    type="text"
                    placeholder="Enter account number"
                    className="h-12 rounded-xl border-gray-200"
                  />
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">SWIFT/IBAN Code</Label>
                  <Input
                    placeholder="Enter SWIFT or IBAN code"
                    className="h-12 rounded-xl border-gray-200"
                  />
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

                <div className="bg-indigo-50 rounded-xl p-4 space-y-2">
                  <p className="text-sm text-gray-700 mb-2">Fee Breakdown</p>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Transfer Amount</span>
                    <span className="text-gray-900">{amount || '0.00'} {selectedCurrency}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Transfer Fee (1%)</span>
                    <span className="text-gray-900">{fee.toFixed(2)} {selectedCurrency}</span>
                  </div>
                  <div className="h-px bg-gray-300 my-2" />
                  <div className="flex justify-between">
                    <span className="text-gray-900">Total</span>
                    <span className="text-gray-900">{totalAmount.toFixed(2)} {selectedCurrency}</span>
                  </div>
                </div>

                <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 flex gap-2">
                  <AlertCircle className="text-amber-600 flex-shrink-0 mt-0.5" size={18} />
                  <p className="text-xs text-amber-800">
                    International transfers may take 1-3 business days to process
                  </p>
                </div>

                <Button 
                  className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700"
                >
                  Send Remittance
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="status" className="mt-0 space-y-3">
            {transactions.map((tx) => (
              <Card key={tx.id} className="p-4 border-0 shadow-md">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                      tx.status === 'completed' ? 'bg-green-100' :
                      tx.status === 'processing' ? 'bg-blue-100' : 'bg-amber-100'
                    }`}>
                      {tx.status === 'completed' ? (
                        <CheckCircle2 className="text-green-600" size={20} />
                      ) : tx.status === 'processing' ? (
                        <Clock className="text-blue-600" size={20} />
                      ) : (
                        <Clock className="text-amber-600" size={20} />
                      )}
                    </div>
                    <div>
                      <p className="text-gray-900">{tx.recipient}</p>
                      <p className="text-xs text-gray-500">{tx.bank}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-gray-900">{tx.amount} {tx.currency}</p>
                    <p className="text-xs text-gray-500">{tx.date}</p>
                  </div>
                </div>

                {tx.status === 'processing' && (
                  <div className="space-y-2">
                    <div className="flex justify-between text-xs text-gray-600">
                      <span>Processing</span>
                      <span>60%</span>
                    </div>
                    <Progress value={60} className="h-2" />
                  </div>
                )}

                {tx.status === 'pending' && (
                  <div className="space-y-2">
                    <div className="flex justify-between text-xs text-gray-600">
                      <span>Pending verification</span>
                      <span>30%</span>
                    </div>
                    <Progress value={30} className="h-2" />
                  </div>
                )}

                {tx.status === 'completed' && (
                  <div className="bg-green-50 rounded-lg p-2 mt-2">
                    <p className="text-xs text-green-700">✓ Transfer completed successfully</p>
                  </div>
                )}
              </Card>
            ))}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
import React, { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ArrowLeft, Building2, Clock, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { Progress } from '../ui/progress';

const API_BASE_URL = 'https://campuscross-multi-wallet-latest.onrender.com/api';

interface RemittanceProps {
  onBack: () => void;
}

export function Remittance({ onBack }: RemittanceProps) {
  const [activeTab, setActiveTab] = useState('send');
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [amount, setAmount] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [bankName, setBankName] = useState('');
  const [accountNumber, setAccountNumber] = useState('');
  const [swiftCode, setSwiftCode] = useState('');
  const [theme] = useState('light');
  const [role] = useState<'STUDENT' | 'MERCHANT' | 'ADMIN'>('STUDENT');
  const [userId, setUserId] = useState<string>('');
  const [wallets, setWallets] = useState<any[]>([]);
  const [selectedWallet, setSelectedWallet] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [transactions, setTransactions] = useState<any[]>([]);

  useEffect(() => {
    // Get user data from localStorage
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        const user = JSON.parse(userData);
        // Try different possible userId fields
        const userIdValue = user.id || user.userId || user.studentId;
        if (userIdValue) {
          const userIdStr = String(userIdValue);
          setUserId(userIdStr);
          fetchWallets(userIdStr);
          fetchTransactions(userIdStr);
        } else {
          console.error('No valid userId found in user data:', user);
        }
      } catch (err) {
        console.error('Failed to parse user data:', err);
      }
    }
  }, []);

  // Update selected wallet when currency changes
  useEffect(() => {
    if (wallets.length > 0) {
      const wallet = wallets.find((w: any) => w.currencyCode === selectedCurrency);
      if (wallet) {
        setSelectedWallet(wallet);
      } else {
        setSelectedWallet(null);
      }
    }
  }, [selectedCurrency, wallets]);

  const fetchWallets = async (uid: string) => {
    if (!uid || uid === 'undefined') {
      console.error('Invalid userId provided to fetchWallets:', uid);
      return;
    }
    
    try {
      const token = localStorage.getItem('authToken') || localStorage.getItem('token');
      if (!token) {
        console.error('No auth token found');
        return;
      }
      
      const response = await fetch(`${API_BASE_URL}/wallets/user/${uid}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const walletsData = await response.json();
        setWallets(walletsData);
        // Set initial selected wallet based on default currency (USD)
        const usdWallet = walletsData.find((w: any) => w.currencyCode === selectedCurrency);
        if (usdWallet) {
          setSelectedWallet(usdWallet);
        } else if (walletsData.length > 0) {
          setSelectedWallet(walletsData[0]);
          setSelectedCurrency(walletsData[0].currencyCode);
        }
      } else {
        console.error('Failed to fetch wallets:', response.status, response.statusText);
      }
    } catch (err) {
      console.error('Failed to fetch wallets:', err);
    }
  };

  const fetchTransactions = async (uid: string) => {
    if (!uid || uid === 'undefined') {
      console.error('Invalid userId provided to fetchTransactions:', uid);
      return;
    }
    
    try {
      const token = localStorage.getItem('authToken') || localStorage.getItem('token');
      if (!token) {
        console.error('No auth token found');
        return;
      }
      
      const response = await fetch(`${API_BASE_URL}/transactions/user/${uid}?page=0&size=10`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        // Filter for remittance transactions
        const remittances = data.transactions?.filter((t: any) => 
          t.type === 'REMITTANCE_OUTBOUND' || t.type === 'WITHDRAWAL'
        ) || [];
        setTransactions(remittances);
      } else {
        console.error('Failed to fetch transactions:', response.status, response.statusText);
      }
    } catch (err) {
      console.error('Failed to fetch transactions:', err);
    }
  };

  const handleSendRemittance = async () => {
    if (!userId || userId === 'undefined') {
      setError('User not authenticated. Please log in again.');
      return;
    }
    
    if (!recipientName || !bankName || !accountNumber || !amount) {
      setError('Please fill in all required fields');
      return;
    }

    if (!selectedWallet) {
      setError(`No ${selectedCurrency} wallet found. Please create a ${selectedCurrency} wallet first.`);
      return;
    }

    const amountNum = parseFloat(amount);
    if (amountNum <= 0) {
      setError('Amount must be greater than zero');
      return;
    }

    if (selectedWallet.balance < amountNum) {
      setError(`Insufficient balance in ${selectedCurrency} wallet. Available: ${selectedWallet.balance} ${selectedCurrency}`);
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem('authToken') || localStorage.getItem('token');
      if (!token) {
        setError('Authentication token not found. Please log in again.');
        return;
      }
      
      // Using the withdrawal endpoint for remittance
      const response = await fetch(`${API_BASE_URL}/banking/withdraw`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          userId: userId,
          walletId: selectedWallet.walletId,
          amount: amountNum,
          currency: selectedCurrency,
          bankAccountNumber: accountNumber,
          bankName: bankName
        })
      });

      if (response.ok) {
        const result = await response.json();
        setSuccess(`Remittance sent successfully! Transaction ID: ${result.transactionId}`);
        
        // Clear form
        setRecipientName('');
        setBankName('');
        setAccountNumber('');
        setSwiftCode('');
        setAmount('');
        
        // Refresh wallets and transactions
        fetchWallets(userId);
        fetchTransactions(userId);
        
        // Switch to status tab after 2 seconds
        setTimeout(() => {
          setActiveTab('status');
          setSuccess('');
        }, 2000);
      } else {
        const errorData = await response.json();
        setError(errorData.error || 'Failed to send remittance');
      }
    } catch (err: any) {
      setError(err.message || 'Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Use the onBack prop passed from parent component

  const fee = parseFloat(amount || '0') * 0.01;
  const totalAmount = parseFloat(amount || '0') + fee;

  const bgColor = theme === 'dark' ? 'from-blue-900 to-indigo-900' : 'from-blue-50 to-indigo-50';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button 
            onClick={onBack} 
            className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center hover:bg-white/20 transition-colors"
          >
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl font-semibold text-white">Remittance</h1>
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
                {error && (
                  <div className="bg-red-50 border border-red-200 rounded-xl p-3 flex gap-2">
                    <AlertCircle className="text-red-600 flex-shrink-0 mt-0.5" size={18} />
                    <p className="text-sm text-red-800">{error}</p>
                  </div>
                )}

                {success && (
                  <div className="bg-green-50 border border-green-200 rounded-xl p-3 flex gap-2">
                    <CheckCircle2 className="text-green-600 flex-shrink-0 mt-0.5" size={18} />
                    <p className="text-sm text-green-800">{success}</p>
                  </div>
                )}

                {selectedWallet ? (
                  <div className="bg-blue-50 rounded-xl p-3">
                    <p className="text-sm text-gray-700 font-medium">
                      Available Balance: {selectedWallet.balance} {selectedWallet.currencyCode}
                    </p>
                  </div>
                ) : (
                  <div className="bg-amber-50 border border-amber-200 rounded-xl p-3">
                    <p className="text-sm text-amber-700 font-medium">
                      No {selectedCurrency} wallet found. Please create a {selectedCurrency} wallet first.
                    </p>
                  </div>
                )}

                <div className="space-y-2">
                  <Label className="text-gray-700">Recipient Name</Label>
                  <Input
                    placeholder="Full name as per bank account"
                    className="h-12 rounded-xl border-gray-200"
                    value={recipientName}
                    onChange={(e) => setRecipientName(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Bank Name</Label>
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                    <Input
                      placeholder="Enter bank name"
                      className="pl-10 h-12 rounded-xl border-gray-200"
                      value={bankName}
                      onChange={(e) => setBankName(e.target.value)}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Account Number</Label>
                  <Input
                    type="text"
                    placeholder="Enter account number"
                    className="h-12 rounded-xl border-gray-200"
                    value={accountNumber}
                    onChange={(e) => setAccountNumber(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">SWIFT/IBAN Code (Optional)</Label>
                  <Input
                    placeholder="Enter SWIFT or IBAN code"
                    className="h-12 rounded-xl border-gray-200"
                    value={swiftCode}
                    onChange={(e) => setSwiftCode(e.target.value)}
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
                        {wallets.length > 0 ? (
                          wallets.map((wallet) => (
                            <SelectItem key={wallet.walletId} value={wallet.currencyCode}>
                              {wallet.currencyCode} (Balance: {wallet.balance})
                            </SelectItem>
                          ))
                        ) : (
                          <SelectItem value="USD">USD</SelectItem>
                        )}
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
                  <p className="text-sm text-gray-700 mb-2 font-medium">Fee Breakdown</p>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Transfer Amount</span>
                    <span className="text-gray-900 font-medium">{amount || '0.00'} {selectedCurrency}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Transfer Fee (1%)</span>
                    <span className="text-gray-900 font-medium">{fee.toFixed(2)} {selectedCurrency}</span>
                  </div>
                  <div className="h-px bg-gray-300 my-2" />
                  <div className="flex justify-between">
                    <span className="text-gray-900 font-semibold">Total</span>
                    <span className="text-gray-900 font-semibold">{totalAmount.toFixed(2)} {selectedCurrency}</span>
                  </div>
                </div>

                <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 flex gap-2">
                  <AlertCircle className="text-amber-600 flex-shrink-0 mt-0.5" size={18} />
                  <p className="text-xs text-amber-800">
                    International transfers may take 1-3 business days to process
                  </p>
                </div>

                <Button 
                  className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                  onClick={handleSendRemittance}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Processing...
                    </>
                  ) : (
                    'Send Remittance'
                  )}
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="status" className="mt-0 space-y-3">
            {transactions.length === 0 ? (
              <Card className="p-8 border-0 shadow-md text-center">
                <Clock className="mx-auto text-gray-400 mb-3" size={48} />
                <p className="text-gray-500">No remittance transactions yet</p>
              </Card>
            ) : (
              transactions.map((tx) => (
                <Card key={tx.transactionId} className="p-4 border-0 shadow-md bg-white">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center ${
                        tx.status === 'COMPLETED' ? 'bg-green-100' :
                        tx.status === 'PROCESSING' ? 'bg-blue-100' : 'bg-amber-100'
                      }`}>
                        {tx.status === 'COMPLETED' ? (
                          <CheckCircle2 className="text-green-600" size={20} />
                        ) : tx.status === 'PROCESSING' ? (
                          <Clock className="text-blue-600" size={20} />
                        ) : (
                          <Clock className="text-amber-600" size={20} />
                        )}
                      </div>
                      <div>
                        <p className="text-gray-900 font-medium">{tx.description || 'Bank Transfer'}</p>
                        <p className="text-xs text-gray-500">{new Date(tx.createdAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-gray-900 font-semibold">{tx.amount} {tx.currencyCode}</p>
                      <p className="text-xs text-gray-500">{tx.status}</p>
                    </div>
                  </div>

                  {tx.status === 'PROCESSING' && (
                    <div className="space-y-2">
                      <div className="flex justify-between text-xs text-gray-600">
                        <span>Processing</span>
                        <span>60%</span>
                      </div>
                      <Progress value={60} className="h-2" />
                    </div>
                  )}

                  {tx.status === 'PENDING' && (
                    <div className="space-y-2">
                      <div className="flex justify-between text-xs text-gray-600">
                        <span>Pending verification</span>
                        <span>30%</span>
                      </div>
                      <Progress value={30} className="h-2" />
                    </div>
                  )}

                  {tx.status === 'COMPLETED' && (
                    <div className="bg-green-50 rounded-lg p-2 mt-2">
                      <p className="text-xs text-green-700 font-medium">✓ Transfer completed successfully</p>
                    </div>
                  )}
                </Card>
              ))
            )}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
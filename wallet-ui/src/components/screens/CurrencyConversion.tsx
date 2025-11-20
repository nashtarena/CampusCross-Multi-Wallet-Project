import React, { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Switch } from '../ui/switch';
import { ArrowLeft, ArrowDownUp, TrendingUp, TrendingDown, Bell } from 'lucide-react';
import { useAppContext } from '../../App';
import { fxApi } from '../../services/fxApi';
import { walletApi, Wallet } from '../../services/walletApi';
import { toast } from 'sonner';

interface CurrencyConversionProps {
  onBack: () => void;
}

const currencies = [
  { code: 'USD', name: 'US Dollar', symbol: '$', color: '#2ECC71', balance: 2450.75 },
  { code: 'EUR', name: 'Euro', symbol: '€', color: '#9B59B6', balance: 1820.50 },
  { code: 'GBP', name: 'British Pound', symbol: '£', color: '#E67E22', balance: 980.25 },
  { code: 'JPY', name: 'Japanese Yen', symbol: '¥', color: '#E74C3C', balance: 125000 },
  { code: 'INR', name: 'Indian Rupee', symbol: '₹', color: '#F4C542', balance: 45500 }
];

const rates = [
  { from: 'USD', to: 'EUR', rate: 0.92, change: -0.3 },
  { from: 'USD', to: 'GBP', rate: 0.79, change: 0.1 },
  { from: 'USD', to: 'JPY', rate: 149.85, change: 0.5 },
  { from: 'EUR', to: 'GBP', rate: 0.86, change: 0.2 }
];

export function CurrencyConversion({ onBack }: CurrencyConversionProps) {
  const [fromCurrency, setFromCurrency] = useState('USD');
  const [toCurrency, setToCurrency] = useState('EUR');
  const [amount, setAmount] = useState('100');
  const [rateAlertEnabled, setRateAlertEnabled] = useState(false);
  const { theme } = useAppContext();

  const fromCurrencyData = currencies.find(c => c.code === fromCurrency);
  const toCurrencyData = currencies.find(c => c.code === toCurrency);
  const [rate, setRate] = useState<number | null>(null);
  const [loadingRate, setLoadingRate] = useState(false);
  const [rateError, setRateError] = useState<string | null>(null);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [isProcessing, setIsProcessing] = useState(false);

  const convertedAmount = rate ? parseFloat(amount || '0') * rate : 0;

  useEffect(() => {
    // load user's wallets so we can check balances and wallet ids
    const loadWallets = async () => {
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) return;
        const user = JSON.parse(userStr);
        const userWallets = await walletApi.getUserWallets(user.id);
        setWallets(userWallets);
      } catch (err) {
        console.error('Failed to load wallets', err);
      }
    };
    loadWallets();

    let mounted = true;
    async function fetchRate() {
      setLoadingRate(true);
      setRateError(null);
      try {
        const resp = await fxApi.getQuote(fromCurrency, toCurrency);
        if (!mounted) return;
        setRate(Number(resp.rate));
      } catch (err: any) {
        if (!mounted) return;
        setRateError(err.message || 'Failed to fetch rate');
        setRate(null);
      } finally {
        if (mounted) setLoadingRate(false);
      }
    }

    // Only fetch when currencies differ
    if (fromCurrency && toCurrency && fromCurrency !== toCurrency) {
      fetchRate();
    } else if (fromCurrency === toCurrency) {
      setRate(1);
    }

    return () => { mounted = false; };
  }, [fromCurrency, toCurrency]);

  // Refresh wallets helper
  const refreshWallets = async () => {
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr) return;
      const user = JSON.parse(userStr);
      const userWallets = await walletApi.getUserWallets(user.id);
      setWallets(userWallets);
    } catch (err) {
      console.error('Failed to refresh wallets', err);
    }
  };

  const bgColor = theme === 'dark' ? 'from-slate-800 to-slate-900' : 'from-slate-50 to-slate-100';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-[#607D8B] p-6 pb-20 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Currency Conversion</h1>
            <p className="text-sm text-white/80">Real-time exchange rates</p>
          </div>
        </div>
      </div>

      {/* Conversion Card */}
      <div className="px-6 -mt-16 mb-6">
        <Card className="p-6 bg-white shadow-xl border-0">
          {/* From Currency */}
          <div className="mb-4">
            <label className="text-xs text-gray-600 mb-2 block">From</label>
            <div className="flex gap-3">
              <Select value={fromCurrency} onValueChange={setFromCurrency}>
                <SelectTrigger className="w-32 h-14 rounded-xl border-gray-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {currencies.map(currency => (
                    <SelectItem key={currency.code} value={currency.code}>
                      {currency.code}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="flex-1">
                <Input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="h-14 rounded-xl border-gray-200 text-xl"
                  placeholder="0.00"
                />
              </div>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              Available: {fromCurrencyData?.symbol}{fromCurrencyData?.balance.toLocaleString()}
            </p>
          </div>

          {/* Swap Button */}
          <div className="flex justify-center -my-2 relative z-10">
            <button 
              onClick={() => {
                setFromCurrency(toCurrency);
                setToCurrency(fromCurrency);
              }}
              className="w-12 h-12 rounded-full bg-[#607D8B] shadow-lg flex items-center justify-center hover:bg-[#546E7A] transition-colors"
            >
              <ArrowDownUp className="text-white" size={20} />
            </button>
          </div>

          {/* To Currency */}
          <div className="mb-4">
            <label className="text-xs text-gray-600 mb-2 block">To</label>
            <div className="flex gap-3">
              <Select value={toCurrency} onValueChange={setToCurrency}>
                <SelectTrigger className="w-32 h-14 rounded-xl border-gray-200">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {currencies.map(currency => (
                    <SelectItem key={currency.code} value={currency.code}>
                      {currency.code}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="flex-1">
                <div className="h-14 rounded-xl border border-gray-200 bg-gray-50 flex items-center px-4 text-xl text-gray-900">
                  {convertedAmount.toFixed(2)}
                </div>
              </div>
            </div>
          </div>

          {/* Rate Info */}
          <div className="bg-slate-50 rounded-xl p-4 mb-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm text-gray-600">Exchange Rate</span>
              <div className="flex items-center gap-1">
                <TrendingDown className="text-red-500" size={14} />
                <span className="text-xs text-red-500">-0.3%</span>
              </div>
            </div>
            <p className="text-gray-900">
              {loadingRate ? (
                'Loading rate...'
              ) : rateError ? (
                <span className="text-red-600">{rateError}</span>
              ) : (
                <>1 {fromCurrency} = {rate?.toFixed(6)} {toCurrency}</>
              )}
            </p>
            <p className="text-xs text-gray-500 mt-1">{loadingRate ? 'Fetching latest rate' : 'Live rate from FX service'}</p>
          </div>

          {/* Rate Alert */}
          <div className="flex items-center justify-between p-4 bg-amber-50 rounded-xl mb-4">
            <div className="flex items-center gap-3">
              <Bell className="text-amber-600" size={20} />
              <div>
                <p className="text-sm text-gray-900">Rate Alert</p>
                <p className="text-xs text-gray-600">Notify when rate changes</p>
              </div>
            </div>
            <Switch 
              checked={rateAlertEnabled} 
              onCheckedChange={setRateAlertEnabled}
            />
          </div>

          {/* Convert Button */}
          <Button 
            className="w-full h-12 rounded-xl"
            style={{ background: '#607D8B' }}
            onClick={async () => {
              // Basic validation
              const amountNum = Number(amount || 0);
              if (!amountNum || amountNum <= 0) {
                toast.error('Enter a valid amount to convert');
                return;
              }

              if (!rate) {
                toast.error('No exchange rate available');
                return;
              }

              // Find source wallet
              const sourceWallet = wallets.find(w => w.currencyCode === fromCurrency);
              if (!sourceWallet) {
                toast.error(`No ${fromCurrency} wallet found`);
                return;
              }

              // Check balance
              if ((sourceWallet.balance || 0) < amountNum) {
                toast.error('Insufficient funds in source wallet');
                return;
              }

              setIsProcessing(true);
              try {
                // Determine or create target wallet
                let targetWallet = wallets.find(w => w.currencyCode === toCurrency);
                if (!targetWallet) {
                  const created = await walletApi.createWallet('Converted Wallet', toCurrency, false);
                  targetWallet = created;
                  // optimistically add to local list
                  setWallets(prev => [...prev, created]);
                }

                // Compute converted amount (use rate as to-per-1-from)
                const converted = Number((amountNum * (rate || 0)).toFixed(6));

                // Deduct from source
                await walletApi.deductFunds(sourceWallet.id, amountNum, fromCurrency);

                // Add to target
                await walletApi.addFunds(targetWallet.id, converted, toCurrency);

                toast.success('Conversion completed successfully');

                // Refresh wallets/balances
                await refreshWallets();
              } catch (err: any) {
                console.error('Conversion error', err);
                toast.error(err?.message || 'Conversion failed');
              } finally {
                setIsProcessing(false);
              }
            }}
            disabled={isProcessing}
          >
            {isProcessing ? 'Processing...' : 'Convert Now'}
          </Button>
        </Card>
      </div>

      {/* Live Rates */}
    </div>
  );
}
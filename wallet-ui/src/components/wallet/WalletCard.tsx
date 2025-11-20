import React, { useState } from 'react';
import { Card } from '../ui/card';
import { ArrowUpRight, ArrowDownLeft, Eye, EyeOff, Trash2 } from 'lucide-react';
import { Button } from '../ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { toast } from 'sonner';
import { walletApi } from '../../services/walletApi';
import { Wallet } from '../../services/walletApi';

interface WalletCardProps {
  wallet: Wallet;
  isBalanceHidden?: boolean;
  onDelete?: () => void;
  onNavigate?: (screen: string) => void;
  onWalletUpdated?: () => void;
}

export function WalletCard({ wallet, isBalanceHidden = false, onDelete, onNavigate, onWalletUpdated }: WalletCardProps) {
  const [isDeleting, setIsDeleting] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);
  const [showReceiveDialog, setShowReceiveDialog] = useState(false);
  const [receiveAmount, setReceiveAmount] = useState<string>('');
  const [transactionResult, setTransactionResult] = useState<any | null>(null);

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

  const getCurrencyColor = (currencyCode: string) => {
    const colors: { [key: string]: string } = {
      USD: '#2ECC71',
      EUR: '#9B59B6',
      GBP: '#E67E22',
      JPY: '#E74C3C',
      INR: '#F4C542'
    };
    return colors[currencyCode] || '#3498DB';
  };

  const handleDelete = async () => {
    const walletName = wallet.walletName || `${wallet.currencyCode} Wallet`;
    const confirmed = window.confirm(`Are you sure you want to delete "${walletName}"? This action cannot be undone and all funds in this wallet will be permanently lost.`);
    
    if (!confirmed) {
      return;
    }
    
    console.log('Deleting wallet:', wallet.id);
    setIsDeleting(true);
    try {
      await walletApi.deleteWallet(wallet.id);
      toast.success('Wallet deleted successfully');
      onDelete?.();
    } catch (error: any) {
      console.error('Delete error:', error);
      toast.error(error.message || 'Failed to delete wallet');
    } finally {
      setIsDeleting(false);
    }
  };

  const handleTransfer = async (amount: number) => {
    console.log('Transferring funds:', amount, 'to wallet:', wallet.id);
    setIsTransferring(true);
    try {
      // Use the banking deposit endpoint so the backend records a Transaction
      // and adds funds to the user's wallet for the specified currency.
      await walletApi.depositFromBank(wallet.userId ?? (wallet as any).userId, amount, wallet.currencyCode);
      toast.success(`Successfully added ${getCurrencySymbol(wallet.currencyCode)}${amount.toFixed(2)} to wallet`);
      // Notify parent to refresh wallet list/state if provided
      if (onWalletUpdated) onWalletUpdated();
    } catch (error: any) {
      console.error('Transfer error:', error);
      toast.error(error.message || 'Failed to transfer funds');
    } finally {
      setIsTransferring(false);
    }
  };

  const symbol = getCurrencySymbol(wallet.currencyCode);
  const color = getCurrencyColor(wallet.currencyCode);

  return (
    <>
      <Card 
        className="relative overflow-hidden border-0 shadow-lg"
        style={{ background: `linear-gradient(135deg, ${color} 0%, ${color}dd 100%)` }}
      >
        <div className="p-6 text-white">
          <div className="flex justify-between items-start mb-6">
            <div>
              <p className="text-white/80 text-sm">{wallet.walletName || `${wallet.currencyCode} Wallet`}</p>
              <div className="flex items-center gap-2 mt-1">
                <p className="text-white/90 text-sm font-medium">{wallet.currencyCode}</p>
              </div>
              <div className="mt-2">
                {isBalanceHidden ? (
                  <p className="text-2xl">••••••</p>
                ) : (
                  <p className="text-3xl">{symbol}{wallet.balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</p>
                )}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="bg-white/20 backdrop-blur-sm rounded-lg px-3 py-1">
                <p className="text-xs">{wallet.currencyCode}</p>
              </div>
              <Button 
                variant="ghost" 
                size="sm" 
                className="text-white hover:bg-white/20 p-2 h-8 w-8"
                onClick={handleDelete}
                disabled={isDeleting}
              >
                <Trash2 size={16} />
              </Button>
            </div>
          </div>
          
          <div className="flex gap-2">
            <button 
              className="flex-1 bg-white/20 backdrop-blur-sm hover:bg-white/30 transition-colors rounded-lg py-2 px-3 flex items-center justify-center gap-2"
              onClick={() => setShowReceiveDialog(true)}
              disabled={isTransferring}
            >
              <ArrowDownLeft size={16} />
              <span className="text-sm">{isTransferring ? 'Processing...' : 'Receive'}</span>
            </button>
          </div>
        </div>
          </Card>

          {/* Receive dialog instance */}
          <ReceiveDialog
            open={showReceiveDialog}
            onOpenChange={setShowReceiveDialog}
            wallet={wallet}
            onDeposit={(result)=>{
              setTransactionResult(result);
              // Notify parent to refresh wallets
              if (onWalletUpdated) onWalletUpdated();
            }}
          />

          </>
  );
}

// Receive dialog rendered outside the card for clarity
export function ReceiveDialog({ open, onOpenChange, wallet, onDeposit }: { open: boolean; onOpenChange: (v:boolean)=>void; wallet: Wallet; onDeposit?: (result:any)=>void }) {
  const [amount, setAmount] = useState<string>('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) {
      toast.error('Please enter a valid amount');
      return;
    }
    setLoading(true);
    try {
      const result = await walletApi.depositFromBank(wallet.userId ?? (wallet as any).userId, Number(amount), wallet.currencyCode);
      toast.success('Deposit successful');
      onDeposit?.(result);
      onOpenChange(false);
    } catch (err:any) {
      console.error('Deposit error:', err);
      toast.error(err?.message || 'Deposit failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Receive {wallet.currencyCode}</DialogTitle>
          <DialogDescription>
            Add funds to your {wallet.walletName || wallet.currencyCode} wallet via simulated bank deposit.
          </DialogDescription>
        </DialogHeader>

        <div className="py-2 space-y-4">
          <div className="space-y-2">
            <Label>Amount ({wallet.currencyCode})</Label>
            <Input value={amount} onChange={(e)=>setAmount(e.target.value)} className="h-12" />
          </div>

          <div className="flex gap-2">
            <Button onClick={handleSubmit} disabled={loading} className="flex-1">{loading ? 'Processing...' : 'Deposit'}</Button>
            <Button variant="ghost" onClick={()=>onOpenChange(false)} className="flex-1">Cancel</Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

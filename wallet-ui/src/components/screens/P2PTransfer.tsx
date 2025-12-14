// Helper to determine the direction of a P2P transaction
export const getTransactionDirection = (tx: Transaction) => {
  if (tx.type !== 'P2P_TRANSFER') return 'other';
  const userStr = localStorage.getItem('user');
  if (!userStr) return 'other';
  const user = JSON.parse(userStr);
  const userId = String(user.studentId || user.id).trim().toLowerCase();
  const senderId = String(tx.senderStudentId).trim().toLowerCase();
  const recipientId = String(tx.recipientStudentId).trim().toLowerCase();
  if (recipientId === userId) return 'incoming';
  if (senderId === userId) return 'outgoing';
  return 'other';
};

export const getCurrencySymbol = (currency: string) => {
  const symbols: { [key: string]: string } = {
    USD: "$",
    EUR: "€",
    GBP: "£",
    JPY: "¥",
    INR: "₹",
  };
  return symbols[currency] || currency;
};

export const getP2PDescription = (tx: Transaction) => {
  const direction = getTransactionDirection(tx);
  if (direction === 'incoming') {
    // Incoming: X sent you ...
    const sender = tx.senderStudentId || '';
    return `${sender ? `${sender} sent you ` : 'You received '}${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
  } else if (direction === 'outgoing') {
    // Outgoing: You sent ...
    let recipientDisplay = '';
    if (tx.recipientStudentId) {
      recipientDisplay = tx.recipientStudentId;
    } else if (tx.recipient) {
      recipientDisplay = tx.recipient;
    } else {
      recipientDisplay = 'Unknown';
    }
    return `You sent ${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)} to ${recipientDisplay}`;
  } else {
    // Fallback for other cases
    return `${getCurrencySymbol(tx.currencyCode)}${tx.amount.toFixed(2)}`;
  }
};
import React, { useState, useEffect } from "react";
import { Button } from "../ui/button";
import { Card } from "../ui/card";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/tabs";
import {
  ArrowLeft,
  Send,
  Search,
  Clock,
  CheckCircle2,
  Wallet,
} from "lucide-react";
import { Avatar, AvatarFallback } from "../ui/avatar";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "../ui/dialog";
import { toast } from "sonner";
import { useAppContext } from "../../App";
import { walletApi, transactionApi } from "../../services/walletApi";
import { Wallet as WalletType, Transaction } from "../../services/walletApi";

interface P2PTransferProps {
  onBack: () => void;
}

const recentContacts = [
  { id: 1, name: "Sarah Johnson", studentId: "STU123456", avatar: "SJ" },
  { id: 2, name: "Michael Chen", studentId: "STU789012", avatar: "MC" },
  { id: 3, name: "Emma Davis", studentId: "STU345678", avatar: "ED" },
];

export function P2PTransfer({ onBack }: P2PTransferProps) {
  const [selectedWallet, setSelectedWallet] = useState<WalletType | null>(null);
  const [selectedCurrency, setSelectedCurrency] = useState("USD");
  const [amount, setAmount] = useState("");
  const [recipient, setRecipient] = useState("");
  const [note, setNote] = useState("");
  const [activeTab, setActiveTab] = useState("send");
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [wallets, setWallets] = useState<WalletType[]>([]);
  const [isLoadingWallets, setIsLoadingWallets] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>(
    []
  );
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(false);
  const { theme } = useAppContext();
  const [allWalletsBackup, setAllWalletsBackup] = useState<WalletType[] | null>(
    null
  );

  useEffect(() => {
    const fetchWallets = async () => {
      try {
        setIsLoadingWallets(true);
        const userStr = localStorage.getItem("user");
        if (!userStr) {
          toast.error("User not found");
          return;
        }

        const user = JSON.parse(userStr);
        const userWallets = await walletApi.getUserWallets(user.id);

        const activeWallets = userWallets.filter(
          (w) => !w.isFrozen && !w.isClosed
        );
        setWallets(activeWallets);
        setAllWalletsBackup(activeWallets);

        if (activeWallets.length > 0) {
          setSelectedWallet(activeWallets[0]);
          setSelectedCurrency(activeWallets[0].currency);
        }
      } catch (error) {
        console.error("Failed to fetch wallets:", error);
        //toast.error("Failed to load wallets");
      } finally {
        setIsLoadingWallets(false);
      }
    };

    fetchWallets();
  }, []);

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        setIsLoadingTransactions(true);

        const userStr = localStorage.getItem("user");
        if (!userStr) return;

        const user = JSON.parse(userStr);
        const transactions = await transactionApi.getUserTransactions(user.id);

        const transactionsArray = Array.isArray(transactions)
          ? transactions
          : [];

        // Only show transactions where user is sender or recipient
        const relevantP2PTransactions = transactionsArray.filter((tx: any) => {
          if (tx.type !== "P2P_TRANSFER") return false;
          const userId = user.studentId || user.id;
          return (
            tx.senderStudentId?.toLowerCase() === userId?.toLowerCase() ||
            tx.recipientStudentId?.toLowerCase() === userId?.toLowerCase()
          );
        });
        setRecentTransactions(relevantP2PTransactions.slice(0, 10));
        // ...existing code...
      } catch (error) {
        //console.error("Failed to fetch transactions:", error);
      } finally {
        setIsLoadingTransactions(false);
      }
    };

    fetchTransactions();
  }, []);

  const filterWalletsForRecipient = async (identifier: string) => {
    if (!identifier || identifier.trim() === "") {
      // restore full list if we have backup
      if (allWalletsBackup) setWallets(allWalletsBackup);
      return;
    }

    try {
      const info = await transactionApi.getRecipientInfo(identifier.trim());
      // Expected response shape: { role: 'MERCHANT'|'USER', businessCurrencyCode?: 'USD' }
      if (info?.role === "MERCHANT") {
        const merchantCurrency =
          info.businessCurrencyCode ||
          info.businessCurrency ||
          info.businessCurrencyCode?.toUpperCase();

        // prefer matching on wallet.currency or wallet.currencyCode fields depending on your model
        const filtered = (allWalletsBackup || wallets).filter(
          (w) =>
            (w.currency &&
              w.currency.toUpperCase() === merchantCurrency?.toUpperCase()) ||
            (w.currencyCode &&
              w.currencyCode.toUpperCase() === merchantCurrency?.toUpperCase())
        );

        if (filtered.length === 0) {
          // no compatible wallets; show toast and keep full list (user can't pay until they create matching wallet)
          toast.error(
            `No wallets with currency ${merchantCurrency} available for this merchant.`
          );
          // optionally set wallets to empty to force user to create wallet – here we leave full list
          setWallets(allWalletsBackup || wallets);
          // clear selection so user notices
          setSelectedWallet(null);
          return;
        }

        // limit wallet choices to compatible ones
        setWallets(filtered);

        // if current selectedWallet doesn't match, pick first compatible
        if (
          !selectedWallet ||
          !filtered.find((f) => f.id === selectedWallet.id)
        ) {
          setSelectedWallet(filtered[0]);
          setSelectedCurrency(filtered[0].currency || filtered[0].currencyCode);
        }
      } else {
        // Non-merchant: restore full list
        if (allWalletsBackup) setWallets(allWalletsBackup);
      }
    } catch (err) {
      console.error("Failed to lookup recipient for wallet filtering:", err);
      // on error restore list
      if (allWalletsBackup) setWallets(allWalletsBackup);
    }
  };

  const handleSendMoney = async () => {
    if (!selectedWallet) {
      toast.error("Please select a wallet");
      return;
    }
    if (!recipient) {
      toast.error("Please enter a recipient");
      return;
    }
    if (!amount || parseFloat(amount) <= 0) {
      toast.error("Please enter a valid amount");
      return;
    }
    if (parseFloat(amount) > selectedWallet.balance) {
      toast.error("Insufficient balance");
      return;
    }
    /** Merchant currency restriction */
    try {
      const userInfo = await transactionApi.getRecipientInfo(recipient.trim());
      if (userInfo?.role === "MERCHANT") {
        const merchantCurrency = userInfo.businessCurrencyCode;

        const walletCurrency = (
          selectedWallet.currency || selectedWallet.currencyCode
        )?.toUpperCase();
        const merchantCurr = merchantCurrency?.toUpperCase();

        if (walletCurrency !== merchantCurr) {
          toast.error(
            `This merchant only accepts payments in ${merchantCurr}. Choose a wallet with matching currency.`
          );
          return;
        }
      }
    } catch (err) {
      console.error("Failed to validate merchant currency", err);
    }

    setShowConfirmation(true);
  };

  const confirmTransfer = async () => {
    if (!selectedWallet) return;

    try {
      setIsProcessing(true);

      const transferData = {
        sourceWalletId: selectedWallet.id,
        recipientIdentifier: recipient?.trim(),
        amount: parseFloat(amount),
        description: note || `P2P transfer to ${recipient}`,
      };

      await transactionApi.p2pTransfer(transferData);

      toast.success(
        `Successfully sent ${amount} to ${recipient}`
      );
      setShowConfirmation(false);
      setAmount("");
      setRecipient("");
      setNote("");

      const userStr = localStorage.getItem("user");
      if (userStr) {
        const user = JSON.parse(userStr);
        const userWallets = await walletApi.getUserWallets(user.id);
        const activeWallets = userWallets.filter(
          (w) => !w.isFrozen && !w.isClosed
        );
        setWallets(activeWallets);

        const updatedWallet = activeWallets.find(
          (w) => w.id === selectedWallet.id
        );
        if (updatedWallet) {
          setSelectedWallet(updatedWallet);
        }
      }

      const transactions = await transactionApi.getUserTransactions(
        JSON.parse(userStr!).id
      );
      const transactionsArray = Array.isArray(transactions) ? transactions : [];
      const p2pTransactions = transactionsArray.filter(
        (tx: any) => tx.type === "P2P_TRANSFER"
      );
      setRecentTransactions(p2pTransactions.slice(0, 10));
    } catch (error: any) {
      console.error("Transfer failed:", error);
      toast.error(error.message || "Transfer failed");
    } finally {
      setIsProcessing(false);
    }
  };

  const getCurrencySymbol = (currency: string) => {
    const symbols: { [key: string]: string } = {
      USD: "$",
      EUR: "€",
      GBP: "£",
      JPY: "¥",
      INR: "₹",
    };
    return symbols[currency] || currency;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60);

    if (diffInHours < 1) return "Just now";
    if (diffInHours < 24) return `${Math.floor(diffInHours)}h ago`;
    if (diffInHours < 48) return "1d ago";
    return `${Math.floor(diffInHours / 24)}d ago`;
  };

  function navigateBackByRole() {
    try {
      const userStr = localStorage.getItem("user");
      if (!userStr) {
        window.dispatchEvent(
          new CustomEvent("navigate", {
            detail: { screen: "home" },
          })
        );
        return;
      }

      const user = JSON.parse(userStr);
      const role = user.role;

      if (role === "ADMIN") {
        window.dispatchEvent(
          new CustomEvent("navigate", {
            detail: { screen: "admin" },
          })
        );
      } else if (role === "MERCHANT") {
        window.dispatchEvent(
          new CustomEvent("navigate", {
            detail: { screen: "merchant" },
          })
        );
      } else {
        window.dispatchEvent(
          new CustomEvent("navigate", {
            detail: { screen: "home" },
          })
        );
      }
    } catch {
      window.dispatchEvent(
        new CustomEvent("navigate", {
          detail: { screen: "home" },
        })
      );
    }
  }

  const bgColor =
    theme === "dark" ? "from-cyan-900 to-blue-900" : "from-cyan-50 to-blue-50";

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-[#00BCD4] p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button
            onClick={navigateBackByRole}
            className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center"
          >
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
          <TabsContent value="send" className="mt-0 space-y-6">
            <Card className="p-6 bg-white shadow-lg border-0">
              <div className="space-y-4">
                {/* Wallet Select */}
                <div className="space-y-2">
                  <Label className="text-gray-700">Select Wallet</Label>

                  {isLoadingWallets ? (
                    <div className="h-12 rounded-xl border border-gray-200 flex items-center justify-center">
                      <span className="text-gray-500">Loading wallets...</span>
                    </div>
                  ) : wallets.length === 0 ? (
                    <div className="h-12 rounded-xl border border-gray-200 flex items-center justify-center">
                      <span className="text-gray-500">
                        No wallets available
                      </span>
                    </div>
                  ) : (
                    <Select
                      value={selectedWallet?.id.toString()}
                      onValueChange={(value: string) => {
                        const wallet = wallets.find(
                          (w) => w.id === parseInt(value)
                        );
                        if (wallet) {
                          setSelectedWallet(wallet);
                          getCurrencySymbol(wallet.currencyCode);
                        }
                      }}
                    >
                      <SelectTrigger className="h-12 rounded-xl border-gray-200">
                        {/* FIX: Currency symbol + code */}
                        <SelectValue placeholder="Select Wallet">
                          {selectedWallet ? (
                            <span>
                              {getCurrencySymbol(selectedWallet.currency)}{" "}
                              {selectedWallet.balance.toFixed(2)} •{" "}
                              {selectedWallet.walletName}
                            </span>
                          ) : (
                            "Select Wallet"
                          )}
                        </SelectValue>
                      </SelectTrigger>

                      <SelectContent>
                        {wallets.map((wallet) => (
                          <SelectItem
                            key={wallet.id}
                            value={wallet.id.toString()}
                          >
                            <div className="flex items-center gap-2">
                              <Wallet className="w-4 h-4" />
                              <span>
                                {getCurrencySymbol(wallet.currency)}{" "}
                                {wallet.balance.toFixed(2)}
                              </span>

                              {/* FIX: Currency code in dropdown */}
                              <span className="text-gray-600 text-sm font-medium">
                                • {wallet.currency}
                              </span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}

                  {selectedWallet && (
                    <p className="text-xs text-gray-500">
                      Balance: {getCurrencySymbol(selectedWallet.currency)}{" "}
                      {selectedWallet.balance.toFixed(2)}
                    </p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label className="text-gray-700">Recipient</Label>
                  <div className="relative">
                    <Search
                      className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                      size={20}
                    />
                    <Input
                      placeholder="Student ID or Mobile Number"
                      className="pl-10 h-12 rounded-xl border-gray-200"
                      value={recipient}
                      onChange={async (e) => {
                        const val = e.target.value;
                        setRecipient(val);
                        // call filtering (no debounce here for simplicity)
                        await filterWalletsForRecipient(val);
                      }}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="text-gray-700">Currency</Label>
                    <Select
                      value={selectedCurrency}
                      onValueChange={setSelectedCurrency}
                      disabled={!!selectedWallet}
                    >
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
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                  />
                </div>

                <div className="bg-cyan-50 rounded-xl p-4">
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-600">Transfer Fee</span>
                    <span className="text-gray-900">Free</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-900">Total</span>
                    <span className="text-gray-900">
                      {amount || "0.00"} {selectedCurrency}
                    </span>
                  </div>
                </div>

                <Button
                  className="w-full h-12 rounded-xl"
                  style={{ background: "#00BCD4" }}
                  onClick={handleSendMoney}
                  disabled={
                    isProcessing || isLoadingWallets || wallets.length === 0
                  }
                >
                  {isProcessing ? (
                    <div className="flex items-center">
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                      Processing...
                    </div>
                  ) : (
                    <>
                      <Send size={20} className="mr-2" />
                      Send Money
                    </>
                  )}
                </Button>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      <Dialog open={showConfirmation} onOpenChange={setShowConfirmation}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Confirm Transfer</DialogTitle>
            <DialogDescription>
              Are you sure you want to send {amount} {selectedCurrency} to{" "}
              {recipient}?
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-4">
            <Button
              variant="outline"
              onClick={() => setShowConfirmation(false)}
            >
              Cancel
            </Button>
            <Button onClick={confirmTransfer} disabled={isProcessing}>
              {isProcessing ? (
                <div className="flex items-center">
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                  Processing...
                </div>
              ) : (
                "Confirm"
              )}
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

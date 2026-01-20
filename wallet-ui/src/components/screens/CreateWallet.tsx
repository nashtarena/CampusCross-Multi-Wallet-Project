import { useState, useEffect } from "react";
import { ArrowLeft, Plus, Wallet as WalletIcon } from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Card } from "../ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { Switch } from "../ui/switch";
import { toast } from "sonner";
import { walletApi } from "../../services/walletApi";

interface CreateWalletProps {
  onBack: () => void;
  onWalletCreated: () => void;
}

export function CreateWallet({ onBack, onWalletCreated }: CreateWalletProps) {
  const [walletName, setWalletName] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [isDefault, setIsDefault] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [userWallets, setUserWallets] = useState<any[]>([]);
  const [duplicateCurrency, setDuplicateCurrency] = useState(false);

  const currencies = [
    { value: "USD", label: "USD - US Dollar" },
    { value: "EUR", label: "EUR - Euro" },
    { value: "GBP", label: "GBP - British Pound" },
    { value: "JPY", label: "JPY - Japanese Yen" },
    { value: "INR", label: "INR - Indian Rupee" },
  ];

  useEffect(() => {
    // Get userId from localStorage
    const userStr = localStorage.getItem("user");
    if (!userStr) return;
    const user = JSON.parse(userStr);
    const userId = user.userId || user.id || user.studentId;
    if (!userId) return;
    walletApi.getUserWallets(userId)
      .then((wallets) => setUserWallets(wallets))
      .catch(() => setUserWallets([]));
  }, []);

  useEffect(() => {
    // Check for duplicate currency
    const exists = userWallets.some(w => (w.currencyCode || w.currency) === currency);
    setDuplicateCurrency(exists);
  }, [currency, userWallets]);

  const handleCreateWallet = async () => {
    if (!walletName.trim()) {
      toast.error("Please enter a wallet name");
      return;
    }
    if (duplicateCurrency) {
      toast.error("You already have a wallet with this currency. Creation not possible.");
      return;
    }

    setIsLoading(true);
    try {
      await walletApi.createWallet(walletName, currency, isDefault);
      toast.success("Wallet created successfully!");
      onWalletCreated();
    } catch (error) {
      console.error("Create wallet error:", error);
      toast.error(
        error instanceof Error ? error.message : "Failed to create wallet"
      );
    } finally {
      setIsLoading(false);
    }
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

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      {/* Header */}
      <div className="flex items-center gap-4 mb-8">
        <Button
          variant="ghost"
          size="icon"
          onClick={navigateBackByRole}
          className="rounded-full"
        >
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Create New Wallet
          </h1>
          <p className="text-gray-600">Add a new wallet to your account</p>
        </div>
      </div>

      <Card className="p-6 max-w-md mx-auto">
        <div className="space-y-6">
          {/* Wallet Name */}
          <div className="space-y-2">
            <Label htmlFor="walletName">Wallet Name</Label>
            <Input
              id="walletName"
              placeholder="e.g., My Savings Wallet"
              value={walletName}
              onChange={(e) => setWalletName(e.target.value)}
              className="w-full"
            />
          </div>

          {/* Currency Selection */}
          <div className="space-y-2">
            <Label htmlFor="currency">Currency</Label>
            <Select value={currency} onValueChange={setCurrency}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Select currency" />
              </SelectTrigger>
              <SelectContent>
                {currencies.map((curr) => (
                  <SelectItem key={curr.value} value={curr.value}>
                    {curr.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Create Button */}
          <Button
            onClick={handleCreateWallet}
            disabled={isLoading || !walletName.trim() || duplicateCurrency}
            className="w-full"
          >
            {isLoading ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                Creating Wallet...
              </>
            ) : (
              <>
                <Plus className="w-4 h-4 mr-2" />
                Create Wallet
              </>
            )}
          </Button>
          {duplicateCurrency && (
            <div className="text-red-600 text-sm mt-2">You already have a wallet with this currency. Creation is not possible.</div>
          )}

          {/* Info Section */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start gap-3">
              <WalletIcon className="w-5 h-5 text-blue-600 mt-0.5" />
              <div>
                <h4 className="font-semibold text-blue-900 mb-1">
                  Wallet Information
                </h4>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>• Personal wallets have daily and monthly limits</li>
                  <li>
                    • You can create multiple wallets in different currencies
                  </li>
                  <li>• Default wallet is used for quick transactions</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}

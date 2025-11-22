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
import { Switch } from "../ui/switch";
import {
  ArrowLeft,
  Bell,
  Plus,
  Trash2,
  TrendingUp,
  TrendingDown,
} from "lucide-react";
import { Badge } from "../ui/badge";

// Backend alert structure - matches Java RateAlert entity
interface BackendAlert {
  id: number;
  userId: number;
  currencyPair: string; // e.g., "USD/EUR"
  thresholdValue: number;
  direction: "ABOVE" | "BELOW";
  status: "ACTIVE" | "INACTIVE" | "TRIGGERED";
  createdAt?: string;
}

// Frontend display structure
interface FrontendAlert {
  id: number;
  from: string;
  to: string;
  direction: "ABOVE" | "BELOW";
  rate: number;
  currentRate: number;
  active: boolean;
  status: "ACTIVE" | "INACTIVE" | "TRIGGERED";
}

interface RateAlertsProps {
  onBack: () => void;
  userId?: number; // optional; component will fallback to localStorage.user if not provided
}

// API Service Functions
const API_BASE_URL =
  import.meta.env.VITE_API_URL || "https://campuscross-multi-wallet-project-naif.onrender.com/api/v1";

const alertService = {
  // Create new alert
  createAlert: async (
    alert: Omit<BackendAlert, "id" | "createdAt">
  ): Promise<BackendAlert> => {
    const response = await fetch(`${API_BASE_URL}/alerts`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(alert),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to create alert: ${errorText}`);
    }

    return response.json();
  },

  // Get alerts for user
  getAlertsByUserId: async (userId: number): Promise<BackendAlert[]> => {
    const response = await fetch(`${API_BASE_URL}/alerts/user/${userId}`);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to fetch alerts: ${errorText}`);
    }

    return response.json();
  },

  // Delete alert
  deleteAlert: async (alertId: number): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/alerts/${alertId}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Failed to delete alert: ${errorText}`);
    }
  },
};

// Helper function to get current rate from backend FX service
const getCurrentRate = async (from: string, to: string): Promise<number> => {
  try {
    const response = await fetch(`${API_BASE_URL}/fx/quote/${from}/${to}`);

    if (!response.ok) {
      console.error(`Failed to fetch rate for ${from}/${to}`);
      return 0;
    }

    const data = await response.json();
    // Backend returns { from: "USD", to: "EUR", rate: 0.85 }
    return data.rate || 0;
  } catch (error) {
    console.error(`Error fetching rate for ${from}/${to}:`, error);
    return 0;
  }
};

// Transform backend alert to frontend format
const transformBackendToFrontend = (
  backendAlert: BackendAlert,
  currentRates: Map<string, number>
): FrontendAlert => {
  const [from, to] = backendAlert.currencyPair.split("/");
  const currentRate = currentRates.get(backendAlert.currencyPair) || 0;

  return {
    id: backendAlert.id,
    from,
    to,
    direction: backendAlert.direction,
    rate: backendAlert.thresholdValue,
    currentRate,
    active: backendAlert.status === "ACTIVE", // Only ACTIVE alerts are considered active
    status: backendAlert.status, // Pass through the actual status from backend
  };
};

// Transform frontend input to backend format
const transformFrontendToBackend = (
  userId: number,
  from: string,
  to: string,
  direction: "ABOVE" | "BELOW",
  targetRate: string,
  active: boolean = true
): Omit<BackendAlert, "id" | "createdAt"> => {
  return {
    userId,
    currencyPair: `${from}/${to}`,
    thresholdValue: parseFloat(targetRate),
    direction,
    status: active ? "ACTIVE" : "INACTIVE",
  };
};

export function RateAlerts({ onBack, userId }: RateAlertsProps) {
  // Resolve the user id: prefer the prop, otherwise read from localStorage.user
  const resolveUserId = (): number | null => {
    if (typeof userId === "number" && !Number.isNaN(userId) && userId > 0) return userId;
    try {
      const userStr = localStorage.getItem("user");
      if (!userStr) return null;
      const user = JSON.parse(userStr);
      const id = Number(user?.id ?? user?.userId ?? user?.studentId);
      return Number.isFinite(id) && id > 0 ? id : null;
    } catch (err) {
      return null;
    }
  };

  const resolvedUserId = resolveUserId();
  if (!resolvedUserId) {
    // Not fatal, but warn so developers notice missing id
    // eslint-disable-next-line no-console
    console.warn("RateAlerts: no userId provided and none found in localStorage.user");
  }
  const [fromCurrency, setFromCurrency] = useState("USD");
  const [toCurrency, setToCurrency] = useState("EUR");
  const [direction, setDirection] = useState<"ABOVE" | "BELOW">("ABOVE");
  const [targetRate, setTargetRate] = useState("");

  const [alerts, setAlerts] = useState<FrontendAlert[]>([]);
  const [currentRates, setCurrentRates] = useState<Map<string, number>>(
    new Map()
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load alerts on component mount or when resolved user id changes
  useEffect(() => {
    if (!resolvedUserId) {
      setAlerts([]);
      return;
    }
    loadAlerts();
  }, [resolvedUserId]);

  // Load current rates periodically
  useEffect(() => {
    const updateRates = async () => {
      const uniquePairs = [...new Set(alerts.map((a) => `${a.from}/${a.to}`))];
      const rateMap = new Map<string, number>();

      for (const pair of uniquePairs) {
        const [from, to] = pair.split("/");
        try {
          const rate = await getCurrentRate(from, to);
          rateMap.set(pair, rate);
        } catch (err) {
          console.error(`Failed to fetch rate for ${pair}`, err);
        }
      }

      setCurrentRates(rateMap);

      // Update alerts with new rates
      setAlerts((prevAlerts) =>
        prevAlerts.map((alert) => ({
          ...alert,
          currentRate:
            rateMap.get(`${alert.from}/${alert.to}`) || alert.currentRate,
        }))
      );
    };

    if (alerts.length > 0) {
      updateRates();
      const interval = setInterval(updateRates, 30000); // Update every 30 seconds
      return () => clearInterval(interval);
    }
  }, [alerts.length]);

  const loadAlerts = async () => {
    setLoading(true);
    setError(null);

    try {
      if (!resolvedUserId) throw new Error("No user id available");
      const backendAlerts = await alertService.getAlertsByUserId(resolvedUserId);

      // Fetch current rates for all currency pairs
      const uniquePairs = [
        ...new Set(backendAlerts.map((a) => a.currencyPair)),
      ];
      const rateMap = new Map<string, number>();

      for (const pair of uniquePairs) {
        const [from, to] = pair.split("/");
        try {
          const rate = await getCurrentRate(from, to);
          rateMap.set(pair, rate);
        } catch (err) {
          console.error(`Failed to fetch rate for ${pair}`, err);
        }
      }

      setCurrentRates(rateMap);
      const frontendAlerts = backendAlerts.map((a) =>
        transformBackendToFrontend(a, rateMap)
      );
      setAlerts(frontendAlerts);
    } catch (err) {
      setError("Failed to load alerts. Please try again.");
      console.error("Error loading alerts:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAlert = async () => {
    if (!targetRate || parseFloat(targetRate) <= 0) {
      setError("Please enter a valid target rate");
      return;
    }

    if (fromCurrency === toCurrency) {
      setError("Please select different currencies");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      if (!resolvedUserId) throw new Error("No user id available");
      const newAlert = transformFrontendToBackend(
        resolvedUserId,
        fromCurrency,
        toCurrency,
        direction,
        targetRate
      );

      await alertService.createAlert(newAlert);

      // Reset form
      setTargetRate("");

      // Reload alerts
      await loadAlerts();
    } catch (err) {
      setError("Failed to create alert. Please try again.");
      console.error("Error creating alert:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAlert = async (alertId: number) => {
    setLoading(true);
    setError(null);

    try {
      await alertService.deleteAlert(alertId);
      await loadAlerts();
    } catch (err) {
      setError("Failed to delete alert. Please try again.");
      console.error("Error deleting alert:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleAlert = async (alert: FrontendAlert) => {
    // Since backend doesn't have a PATCH endpoint, we delete and recreate
    setLoading(true);
    setError(null);

    try {
      // Delete existing alert
      await alertService.deleteAlert(alert.id);

      // Create new alert with toggled status
      if (!resolvedUserId) throw new Error("No user id available");
      const newAlert = transformFrontendToBackend(
        resolvedUserId,
        alert.from,
        alert.to,
        alert.direction,
        alert.rate.toString(),
        !alert.active
      );

      await alertService.createAlert(newAlert);
      await loadAlerts();
    } catch (err) {
      setError("Failed to toggle alert. Please try again.");
      console.error("Error toggling alert:", err);
    } finally {
      setLoading(false);
    }
  };

  const activeAlertsCount = alerts.filter((a) => a.active).length;

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
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button
            onClick={navigateBackByRole}
            className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center"
          >
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Rate Alerts</h1>
            <p className="text-sm text-white/80">Get notified about FX rates</p>
          </div>
        </div>

        <div className="bg-white/10 backdrop-blur-sm rounded-2xl p-4 flex items-center gap-3">
          <div className="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center">
            <Bell className="text-white" size={24} />
          </div>
          <div>
            <p className="text-white">
              {activeAlertsCount} Active Alert
              {activeAlertsCount !== 1 ? "s" : ""}
            </p>
            <p className="text-xs text-white/80">
              Monitoring exchange rates for you
            </p>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="px-6 mt-4">
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
            {error}
          </div>
        </div>
      )}

      {/* Create Alert */}
      <div className="px-6 -mt-4 mb-6">
        <Card className="p-6 bg-white shadow-lg border-0">
          <h3 className="text-gray-900 mb-4 flex items-center gap-2">
            <Plus size={20} />
            Create New Alert
          </h3>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label className="text-gray-700">From Currency</Label>
                <Select value={fromCurrency} onValueChange={setFromCurrency}>
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
                <Label className="text-gray-700">To Currency</Label>
                <Select value={toCurrency} onValueChange={setToCurrency}>
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
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label className="text-gray-700">Direction</Label>
                <Select
                  value={direction}
                  onValueChange={(val) =>
                    setDirection(val as "ABOVE" | "BELOW")
                  }
                >
                  <SelectTrigger className="h-12 rounded-xl border-gray-200">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ABOVE">Goes Above</SelectItem>
                    <SelectItem value="BELOW">Goes Below</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label className="text-gray-700">Target Rate</Label>
                <Input
                  type="number"
                  step="0.0001"
                  placeholder="0.0000"
                  className="h-12 rounded-xl border-gray-200"
                  value={targetRate}
                  onChange={(e) => setTargetRate(e.target.value)}
                />
              </div>
            </div>

            <Button
              className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700"
              onClick={handleCreateAlert}
              disabled={loading}
            >
              {loading ? "Creating..." : "Create Alert"}
            </Button>
          </div>
        </Card>
      </div>

      {/* Active Alerts */}
      <div className="px-6 pb-6">
        <h3 className="text-gray-900 mb-3">
          {loading && alerts.length === 0
            ? "Loading alerts..."
            : "Active Alerts"}
        </h3>

        {alerts.length === 0 && !loading ? (
          <Card className="p-6 bg-white shadow-md border-0 text-center">
            <p className="text-gray-500">
              No alerts yet. Create your first alert above!
            </p>
          </Card>
        ) : (
          <div className="space-y-3">
            {alerts.map((alert) => (
              <Card key={alert.id} className="p-4 bg-white shadow-md border-0">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <p className="text-gray-900">
                        {alert.from}/{alert.to}
                      </p>
                      {(() => {
                        const isTriggered =
                          alert.currentRate > 0 &&
                          ((alert.direction === "ABOVE" &&
                            alert.currentRate > alert.rate) ||
                            (alert.direction === "BELOW" &&
                              alert.currentRate < alert.rate));

                        if (isTriggered) {
                          return (
                            <Badge className="bg-orange-100 text-orange-700">
                              Triggered
                            </Badge>
                          );
                        }

                        return (
                          <Badge
                            variant={alert.active ? "default" : "secondary"}
                            className={
                              alert.active
                                ? "bg-green-100 text-green-700"
                                : "bg-gray-100 text-gray-600"
                            }
                          >
                            {alert.active ? "Active" : "Inactive"}
                          </Badge>
                        );
                      })()}
                    </div>
                    <p className="text-sm text-gray-600">
                      Alert when rate goes {alert.direction.toLowerCase()}{" "}
                      {alert.rate.toFixed(4)}
                    </p>
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-xs text-gray-500">
                        Current rate:
                      </span>
                      <span className="text-sm text-gray-900">
                        {alert.currentRate > 0
                          ? alert.currentRate.toFixed(4)
                          : "Loading..."}
                      </span>
                      {alert.currentRate > 0 &&
                        (alert.currentRate > alert.rate ? (
                          <TrendingUp className="text-green-500" size={14} />
                        ) : (
                          <TrendingDown className="text-red-500" size={14} />
                        ))}
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Switch
                      checked={alert.active}
                      onCheckedChange={() => handleToggleAlert(alert)}
                      disabled={loading}
                    />
                    <button
                      className="text-red-500 hover:bg-red-50 p-2 rounded-lg disabled:opacity-50"
                      onClick={() => handleDeleteAlert(alert.id)}
                      disabled={loading}
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

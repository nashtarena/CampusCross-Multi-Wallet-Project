import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { Switch } from '../ui/switch';
import { ArrowLeft, Bell, Plus, Trash2, TrendingUp, TrendingDown } from 'lucide-react';
import { Badge } from '../ui/badge';

interface RateAlertsProps {
  onBack: () => void;
}

const activeAlerts = [
  { id: 1, from: 'USD', to: 'EUR', condition: 'above', rate: 0.95, currentRate: 0.92, active: true },
  { id: 2, from: 'GBP', to: 'USD', condition: 'below', rate: 1.25, currentRate: 1.27, active: true },
  { id: 3, from: 'USD', to: 'JPY', condition: 'above', rate: 150, currentRate: 149.85, active: false }
];

export function RateAlerts({ onBack }: RateAlertsProps) {
  const [fromCurrency, setFromCurrency] = useState('USD');
  const [toCurrency, setToCurrency] = useState('EUR');
  const [condition, setCondition] = useState('above');
  const [targetRate, setTargetRate] = useState('');

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-4">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
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
            <p className="text-white">3 Active Alerts</p>
            <p className="text-xs text-white/80">Monitoring exchange rates for you</p>
          </div>
        </div>
      </div>

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
                <Label className="text-gray-700">Condition</Label>
                <Select value={condition} onValueChange={setCondition}>
                  <SelectTrigger className="h-12 rounded-xl border-gray-200">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="above">Goes Above</SelectItem>
                    <SelectItem value="below">Goes Below</SelectItem>
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
            >
              Create Alert
            </Button>
          </div>
        </Card>
      </div>

      {/* Active Alerts */}
      <div className="px-6 pb-6">
        <h3 className="text-gray-900 mb-3">Active Alerts</h3>
        <div className="space-y-3">
          {activeAlerts.map((alert) => (
            <Card key={alert.id} className="p-4 bg-white shadow-md border-0">
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <p className="text-gray-900">
                      {alert.from}/{alert.to}
                    </p>
                    <Badge 
                      variant={alert.active ? 'default' : 'secondary'}
                      className={alert.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}
                    >
                      {alert.active ? 'Active' : 'Inactive'}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-600">
                    Alert when rate goes {alert.condition} {alert.rate}
                  </p>
                  <div className="flex items-center gap-2 mt-2">
                    <span className="text-xs text-gray-500">Current rate:</span>
                    <span className="text-sm text-gray-900">{alert.currentRate}</span>
                    {alert.currentRate > alert.rate ? (
                      <TrendingUp className="text-green-500" size={14} />
                    ) : (
                      <TrendingDown className="text-red-500" size={14} />
                    )}
                  </div>
                </div>
                <div className="flex gap-2">
                  <Switch checked={alert.active} />
                  <button className="text-red-500 hover:bg-red-50 p-2 rounded-lg">
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}

import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { ArrowLeft, TrendingUp, ArrowUpRight, ArrowDownLeft, Download, Filter } from 'lucide-react';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Label } from '../ui/label';
import { Checkbox } from '../ui/checkbox';
import { toast } from 'sonner@2.0.3';
import { useAppContext } from '../../App';

interface AnalyticsProps {
  onBack: () => void;
}

const spendingData = [
  { month: 'Jun', amount: 1200 },
  { month: 'Jul', amount: 1800 },
  { month: 'Aug', amount: 1500 },
  { month: 'Sep', amount: 2200 },
  { month: 'Oct', amount: 1900 },
  { month: 'Nov', amount: 2450 }
];

const categoryData = [
  { name: 'Food', value: 850, color: '#E67E22' },
  { name: 'Books', value: 450, color: '#3498DB' },
  { name: 'Transport', value: 320, color: '#9B59B6' },
  { name: 'Shopping', value: 530, color: '#E74C3C' },
  { name: 'Others', value: 300, color: '#95A5A6' }
];

const currencyData = [
  { currency: 'USD', balance: 2450.75, change: 5.2 },
  { currency: 'EUR', balance: 1820.50, change: -2.3 },
  { currency: 'GBP', balance: 980.25, change: 3.1 },
  { currency: 'JPY', balance: 125000, change: 1.8 },
  { currency: 'INR', balance: 45500, change: 4.5 }
];

export function Analytics({ onBack }: AnalyticsProps) {
  const [timeRange, setTimeRange] = useState('6m');
  const [showFilterDialog, setShowFilterDialog] = useState(false);
  const [filters, setFilters] = useState({
    food: true,
    books: true,
    transport: true,
    shopping: true,
    others: true
  });
  const { theme } = useAppContext();

  const handleDownload = () => {
    toast.success('Downloading analytics report...');
    // Simulate download
    setTimeout(() => {
      toast.success('Report downloaded successfully!');
    }, 1500);
  };

  const handleApplyFilters = () => {
    toast.success('Filters applied successfully');
    setShowFilterDialog(false);
  };

  const bgColor = theme === 'dark' ? 'from-blue-900 to-indigo-900' : 'from-blue-50 to-indigo-50';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-[#4682B4] p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Analytics</h1>
            <p className="text-sm text-white/80">Track your spending patterns</p>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 gap-3">
          <Card className="p-4 bg-white/10 backdrop-blur-sm border-white/20">
            <div className="flex items-center gap-2 mb-2">
              <ArrowUpRight className="text-white" size={16} />
              <p className="text-xs text-white/80">Total Spent</p>
            </div>
            <p className="text-2xl text-white">$8,250</p>
            <p className="text-xs text-green-300 mt-1">+12.5% vs last month</p>
          </Card>

          <Card className="p-4 bg-white/10 backdrop-blur-sm border-white/20">
            <div className="flex items-center gap-2 mb-2">
              <ArrowDownLeft className="text-white" size={16} />
              <p className="text-xs text-white/80">Avg Transaction</p>
            </div>
            <p className="text-2xl text-white">$127</p>
            <p className="text-xs text-white/60 mt-1">Last 30 days</p>
          </Card>
        </div>
      </div>

      {/* Filters */}
      <div className="px-6 -mt-4 mb-6">
        <Card className="p-4 bg-white shadow-lg border-0">
          <div className="flex items-center gap-3">
            <Select value={timeRange} onValueChange={setTimeRange}>
              <SelectTrigger className="flex-1 h-10 rounded-lg border-gray-200">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1m">Last Month</SelectItem>
                <SelectItem value="3m">Last 3 Months</SelectItem>
                <SelectItem value="6m">Last 6 Months</SelectItem>
                <SelectItem value="1y">Last Year</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="outline" size="sm" className="rounded-lg" onClick={() => setShowFilterDialog(true)}>
              <Filter size={16} className="mr-2" />
              Filter
            </Button>
            <Button variant="outline" size="sm" className="rounded-lg" onClick={handleDownload}>
              <Download size={16} />
            </Button>
          </div>
        </Card>
      </div>

      {/* Spending Trend */}
      <div className="px-6 mb-6">
        <h3 className="text-gray-900 mb-3">Spending Trend</h3>
        <Card className="p-6 bg-white shadow-lg border-0">
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={spendingData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
              <XAxis dataKey="month" stroke="#6B7280" style={{ fontSize: '12px' }} />
              <YAxis stroke="#6B7280" style={{ fontSize: '12px' }} />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'white', 
                  border: 'none', 
                  borderRadius: '8px', 
                  boxShadow: '0 4px 6px rgba(0,0,0,0.1)' 
                }}
              />
              <Line 
                type="monotone" 
                dataKey="amount" 
                stroke="#4682B4" 
                strokeWidth={3}
                dot={{ fill: '#4682B4', r: 4 }}
                activeDot={{ r: 6 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Category Breakdown */}
      <div className="px-6 mb-6">
        <h3 className="text-gray-900 mb-3">Spending by Category</h3>
        <Card className="p-6 bg-white shadow-lg border-0">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={categoryData}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={80}
                paddingAngle={2}
                dataKey="value"
              >
                {categoryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'white', 
                  border: 'none', 
                  borderRadius: '8px', 
                  boxShadow: '0 4px 6px rgba(0,0,0,0.1)' 
                }}
              />
            </PieChart>
          </ResponsiveContainer>
          
          <div className="grid grid-cols-2 gap-3 mt-4">
            {categoryData.map((cat) => (
              <div key={cat.name} className="flex items-center gap-2">
                <div 
                  className="w-3 h-3 rounded-full" 
                  style={{ backgroundColor: cat.color }}
                />
                <div className="flex-1">
                  <p className="text-xs text-gray-600">{cat.name}</p>
                  <p className="text-sm text-gray-900">${cat.value}</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Currency Performance */}
      <div className="px-6 pb-6">
        <h3 className="text-gray-900 mb-3">Currency Performance</h3>
        <div className="space-y-3">
          {currencyData.map((currency) => (
            <Card key={currency.currency} className="p-4 bg-white shadow-md border-0">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-gray-900">{currency.currency}</p>
                  <p className="text-sm text-gray-600 mt-1">
                    Balance: {currency.balance.toLocaleString()}
                  </p>
                </div>
                <div className="text-right">
                  <div className={`flex items-center gap-1 ${currency.change > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    <TrendingUp size={16} />
                    <span className="text-sm">
                      {currency.change > 0 ? '+' : ''}{currency.change}%
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">This month</p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Filter Dialog */}
      <Dialog open={showFilterDialog} onOpenChange={setShowFilterDialog}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Filter Categories</DialogTitle>
            <DialogDescription>
              Select the categories you want to include in the analytics.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Checkbox
                id="food"
                checked={filters.food}
                onCheckedChange={(checked) => setFilters({ ...filters, food: checked })}
              />
              <Label htmlFor="food">Food</Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="books"
                checked={filters.books}
                onCheckedChange={(checked) => setFilters({ ...filters, books: checked })}
              />
              <Label htmlFor="books">Books</Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="transport"
                checked={filters.transport}
                onCheckedChange={(checked) => setFilters({ ...filters, transport: checked })}
              />
              <Label htmlFor="transport">Transport</Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="shopping"
                checked={filters.shopping}
                onCheckedChange={(checked) => setFilters({ ...filters, shopping: checked })}
              />
              <Label htmlFor="shopping">Shopping</Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="others"
                checked={filters.others}
                onCheckedChange={(checked) => setFilters({ ...filters, others: checked })}
              />
              <Label htmlFor="others">Others</Label>
            </div>
          </div>
          <div className="mt-4">
            <Button variant="outline" size="sm" className="rounded-lg" onClick={handleApplyFilters}>
              Apply Filters
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { ArrowLeft, QrCode, Wifi, MapPin, Coffee, Book, Utensils, Store, CheckCircle2, Camera } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { toast } from 'sonner@2.0.3';
import { useAppContext } from '../../App';

interface CampusPaymentsProps {
  onBack: () => void;
}

const merchants = [
  { id: 1, name: 'Campus Cafeteria', category: 'Food', icon: Utensils, distance: '0.2 km', color: 'from-orange-400 to-orange-600' },
  { id: 2, name: 'University Bookstore', category: 'Books', icon: Book, distance: '0.5 km', color: 'from-blue-400 to-blue-600' },
  { id: 3, name: 'Student Coffee Shop', category: 'Cafe', icon: Coffee, distance: '0.3 km', color: 'from-amber-400 to-amber-600' },
  { id: 4, name: 'Campus Store', category: 'Supplies', icon: Store, distance: '0.7 km', color: 'from-purple-400 to-purple-600' }
];

export function CampusPayments({ onBack }: CampusPaymentsProps) {
  const [showQR, setShowQR] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showScanQR, setShowScanQR] = useState(false);
  const [scanningQR, setScanningQR] = useState(false);
  const [showNFCPayment, setShowNFCPayment] = useState(false);
  const { theme } = useAppContext();

  const handleScanQR = async () => {
    try {
      // Request camera permission
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      
      // Stop the stream immediately (we're just checking permission)
      stream.getTracks().forEach(track => track.stop());
      
      setScanningQR(true);
      toast.success('Camera access granted');
      
      // Simulate QR code scanning
      setTimeout(() => {
        setScanningQR(false);
        setShowScanQR(false);
        setShowSuccess(true);
        setTimeout(() => setShowSuccess(false), 2000);
      }, 2000);
    } catch (error) {
      toast.error('Camera access denied. Please allow camera access to scan QR codes.');
    }
  };

  const handleNFCPayment = () => {
    toast.success('NFC payment initiated. Tap your device to complete payment.');
    setTimeout(() => {
      setShowNFCPayment(false);
      setShowSuccess(true);
      setTimeout(() => setShowSuccess(false), 2000);
    }, 1500);
  };

  const bgColor = theme === 'dark' ? 'from-teal-900 to-emerald-900' : 'from-teal-50 to-emerald-50';

  return (
    <div className={`min-h-screen bg-gradient-to-br ${bgColor}`}>
      {/* Header */}
      <div className="bg-[#009688] p-6 pb-8 rounded-b-3xl">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Campus Payments</h1>
            <p className="text-sm text-white/80">Pay at campus merchants</p>
          </div>
        </div>
      </div>

      {/* Payment Methods */}
      <div className="px-6 -mt-4 mb-6">
        <div className="grid grid-cols-2 gap-3">
          <Card 
            className="p-6 bg-white shadow-lg border-0 cursor-pointer hover:shadow-xl transition-shadow"
            onClick={() => setShowQR(true)}
          >
            <div className="flex flex-col items-center text-center">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-teal-400 to-teal-600 flex items-center justify-center mb-3">
                <QrCode className="text-white" size={32} />
              </div>
              <p className="text-gray-900 mb-1">QR Code</p>
              <p className="text-xs text-gray-600">Scan to pay</p>
            </div>
          </Card>

          <Card 
            className="p-6 bg-white shadow-lg border-0 cursor-pointer hover:shadow-xl transition-shadow"
            onClick={() => setShowNFCPayment(true)}
          >
            <div className="flex flex-col items-center text-center">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-emerald-400 to-emerald-600 flex items-center justify-center mb-3">
                <Wifi className="text-white" size={32} />
              </div>
              <p className="text-gray-900 mb-1">NFC</p>
              <p className="text-xs text-gray-600">Tap to pay</p>
            </div>
          </Card>
        </div>
      </div>

      {/* Nearby Merchants */}
      <div className="px-6 pb-6">
        <div className="flex items-center gap-2 mb-3">
          <MapPin className="text-gray-600" size={18} />
          <h3 className="text-gray-900">Nearby Merchants</h3>
        </div>

        <div className="space-y-3">
          {merchants.map((merchant) => {
            const Icon = merchant.icon;
            return (
              <Card 
                key={merchant.id}
                className="p-4 border-0 shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => {
                  setShowQR(true);
                }}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${merchant.color} flex items-center justify-center flex-shrink-0`}>
                    <Icon className="text-white" size={24} />
                  </div>
                  <div className="flex-1">
                    <p className="text-gray-900">{merchant.name}</p>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-xs text-gray-500">{merchant.category}</span>
                      <span className="text-xs text-gray-400">•</span>
                      <span className="text-xs text-teal-600">{merchant.distance}</span>
                    </div>
                  </div>
                  <Button 
                    size="sm" 
                    className="rounded-lg"
                    style={{ background: '#009688' }}
                  >
                    Pay
                  </Button>
                </div>
              </Card>
            );
          })}
        </div>
      </div>

      {/* QR Code Dialog */}
      <Dialog open={showQR} onOpenChange={setShowQR}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Show QR Code</DialogTitle>
            <DialogDescription>
              Show this code to the merchant to complete your payment
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center py-6">
            <div className="w-64 h-64 bg-white border-4 border-teal-600 rounded-2xl flex items-center justify-center mb-4">
              <QrCode className="text-teal-600" size={200} />
            </div>
            <div className="flex gap-2 w-full">
              <Button 
                className="flex-1 rounded-xl"
                style={{ background: '#009688' }}
                onClick={() => {
                  setShowQR(false);
                  setShowSuccess(true);
                  setTimeout(() => setShowSuccess(false), 2000);
                }}
              >
                Confirm Payment
              </Button>
              <Button 
                variant="outline"
                className="flex-1 rounded-xl"
                onClick={() => setShowScanQR(true)}
              >
                <Camera className="mr-2" size={18} />
                Scan QR
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Scan QR Dialog */}
      <Dialog open={showScanQR} onOpenChange={setShowScanQR}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Scan QR Code</DialogTitle>
            <DialogDescription>
              Position the QR code within the frame to scan
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center py-6">
            <div className="w-64 h-64 bg-gray-900 rounded-2xl flex items-center justify-center mb-4 relative overflow-hidden">
              {scanningQR ? (
                <div className="text-white text-center">
                  <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-white mx-auto mb-4"></div>
                  <p>Scanning...</p>
                </div>
              ) : (
                <Camera className="text-white" size={80} />
              )}
            </div>
            <Button 
              className="w-full rounded-xl"
              style={{ background: '#009688' }}
              onClick={handleScanQR}
              disabled={scanningQR}
            >
              {scanningQR ? 'Scanning...' : 'Start Scanning'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* NFC Payment Dialog */}
      <Dialog open={showNFCPayment} onOpenChange={setShowNFCPayment}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>NFC Payment</DialogTitle>
            <DialogDescription>
              Tap your device on the NFC reader to complete payment
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center py-8">
            <div className="w-32 h-32 rounded-full bg-emerald-100 flex items-center justify-center mb-4 animate-pulse">
              <Wifi className="text-emerald-600" size={64} />
            </div>
            <p className="text-gray-600 text-center mb-6">
              Hold your device near the payment terminal
            </p>
            <Button 
              className="w-full rounded-xl"
              style={{ background: '#009688' }}
              onClick={handleNFCPayment}
            >
              Initiate Payment
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Success Dialog */}
      <Dialog open={showSuccess} onOpenChange={setShowSuccess}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle className="sr-only">Payment Status</DialogTitle>
            <DialogDescription className="sr-only">
              Your payment transaction has been completed successfully
            </DialogDescription>
          </DialogHeader>
          <div className="flex flex-col items-center py-8">
            <div className="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mb-4">
              <CheckCircle2 className="text-green-600" size={48} />
            </div>
            <h3 className="text-xl text-gray-900 mb-2">Payment Successful!</h3>
            <p className="text-gray-600 text-center">
              Your payment has been processed
            </p>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

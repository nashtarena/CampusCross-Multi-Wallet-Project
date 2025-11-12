import React, { useState } from 'react';
import { Button } from '../ui/button';
import { ArrowRight, Wallet, Globe, Shield, Zap, CreditCard } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { useAppContext } from '../../App';

interface WelcomeProps {
  onNext: () => void;
}

export function Welcome({ onNext }: WelcomeProps) {
  const [showSignIn, setShowSignIn] = useState(false);
  const [signInData, setSignInData] = useState({ username: '', password: '' });
  const { setUserName, setUserPassword } = useAppContext();

  const handleSignIn = () => {
    if (signInData.username && signInData.password) {
      setUserName(signInData.username);
      setUserPassword(signInData.password);
      setShowSignIn(false);
      onNext();
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-indigo-900 to-purple-900 flex flex-col relative overflow-hidden">
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-indigo-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute top-60 -left-20 w-60 h-60 bg-purple-500/20 rounded-full blur-3xl animate-pulse delay-1000" />
        <div className="absolute -bottom-20 right-20 w-72 h-72 bg-blue-500/20 rounded-full blur-3xl animate-pulse delay-500" />
      </div>

      {/* Content */}
      <div className="relative z-10 flex-1 flex flex-col">
        {/* Header Section */}
        <div className="pt-16 px-6 text-center">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-3xl bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 mb-6 shadow-2xl shadow-indigo-500/50">
            <Wallet className="text-white" size={40} />
          </div>
          
          <h1 className="text-5xl text-white mb-3 tracking-tight">
            CampusCross
          </h1>
          <p className="text-xl text-indigo-200 mb-2">Multi-Wallet Platform</p>
          <p className="text-sm text-indigo-300/80">Global Financial Solutions for Students</p>
        </div>

        {/* Feature Pills */}
        <div className="px-6 mt-12 flex flex-wrap gap-2 justify-center">
          <div className="px-4 py-2 rounded-full bg-white/10 backdrop-blur-lg border border-white/20">
            <p className="text-xs text-white">5 Currencies</p>
          </div>
          <div className="px-4 py-2 rounded-full bg-white/10 backdrop-blur-lg border border-white/20">
            <p className="text-xs text-white">Instant Transfers</p>
          </div>
          <div className="px-4 py-2 rounded-full bg-white/10 backdrop-blur-lg border border-white/20">
            <p className="text-xs text-white">Bank-Grade Security</p>
          </div>
        </div>

        {/* Main Features Grid */}
        <div className="flex-1 flex items-center px-6 py-12">
          <div className="w-full space-y-4">
            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-5 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                  <Globe className="text-white" size={26} />
                </div>
                <div className="flex-1">
                  <h3 className="text-white text-lg mb-1">Multi-Currency Wallets</h3>
                  <p className="text-indigo-200 text-sm leading-relaxed">
                    Seamlessly manage USD, EUR, GBP, JPY, and INR with real-time exchange rates
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-5 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-purple-400 to-pink-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                  <Zap className="text-white" size={26} />
                </div>
                <div className="flex-1">
                  <h3 className="text-white text-lg mb-1">Lightning Fast P2P</h3>
                  <p className="text-indigo-200 text-sm leading-relaxed">
                    Transfer money to fellow students instantly with zero fees
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-5 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                  <Shield className="text-white" size={26} />
                </div>
                <div className="flex-1">
                  <h3 className="text-white text-lg mb-1">Enterprise Security</h3>
                  <p className="text-indigo-200 text-sm leading-relaxed">
                    256-bit encryption and biometric authentication keep your funds safe
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl p-5 hover:bg-white/15 transition-all duration-300">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center flex-shrink-0 shadow-lg">
                  <CreditCard className="text-white" size={26} />
                </div>
                <div className="flex-1">
                  <h3 className="text-white text-lg mb-1">Campus Payments</h3>
                  <p className="text-indigo-200 text-sm leading-relaxed">
                    Pay at university merchants using QR codes or NFC tap
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* CTA Buttons */}
        <div className="px-6 pb-8 space-y-3">
          <Button 
            onClick={onNext}
            className="w-full bg-white text-indigo-900 hover:bg-indigo-50 rounded-xl h-14 text-lg shadow-2xl shadow-indigo-500/50"
          >
            Get Started
            <ArrowRight className="ml-2" size={22} />
          </Button>
          <Button 
            onClick={() => setShowSignIn(true)}
            variant="ghost"
            className="w-full rounded-xl h-12 text-white hover:bg-white/10 border border-white/20"
          >
            Already have an account? Sign In
          </Button>
        </div>
      </div>

      {/* Sign In Dialog */}
      <Dialog open={showSignIn} onOpenChange={setShowSignIn}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Sign In</DialogTitle>
            <DialogDescription>
              Enter your credentials to access your account
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                type="text"
                placeholder="Enter your username"
                value={signInData.username}
                onChange={(e) => setSignInData({ ...signInData, username: e.target.value })}
                className="h-12 rounded-xl"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="signin-password">Password</Label>
              <Input
                id="signin-password"
                type="password"
                placeholder="Enter your password"
                value={signInData.password}
                onChange={(e) => setSignInData({ ...signInData, password: e.target.value })}
                className="h-12 rounded-xl"
              />
            </div>
            <Button 
              onClick={handleSignIn}
              className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600"
            >
              Sign In
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
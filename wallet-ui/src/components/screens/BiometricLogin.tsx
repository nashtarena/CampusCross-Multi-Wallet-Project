import React, { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Fingerprint, Wallet, Sparkles } from 'lucide-react';

interface BiometricLoginProps {
  onNext: () => void;
}

export function BiometricLogin({ onNext }: BiometricLoginProps) {
  const [scanning, setScanning] = useState(false);
  const [pulseScale, setPulseScale] = useState(1);

  useEffect(() => {
    const interval = setInterval(() => {
      setPulseScale(prev => prev === 1 ? 1.1 : 1);
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleBiometric = () => {
    setScanning(true);
    setTimeout(() => {
      onNext();
    }, 2000);
  };

  return (
    <div className="min-h-screen bg-black flex flex-col relative overflow-hidden">
      {/* Animated Background Grid */}
      <div className="absolute inset-0" style={{
        backgroundImage: `
          linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px),
          linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px)
        `,
        backgroundSize: '60px 60px',
        animation: 'gridMove 20s linear infinite'
      }} />

      {/* Floating Orbs */}
      <div className="absolute top-1/4 left-1/4 w-64 h-64 rounded-full bg-gradient-to-br from-indigo-500/20 to-purple-500/20 blur-3xl animate-pulse" style={{ animationDuration: '3s' }} />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 rounded-full bg-gradient-to-br from-purple-500/20 to-pink-500/20 blur-3xl animate-pulse" style={{ animationDuration: '4s', animationDelay: '1.5s' }} />

      {/* Content */}
      <div className="relative z-10 flex-1 flex flex-col items-center justify-center p-6">
        {/* Logo */}
        <div className="mb-16 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-500 mb-4 shadow-2xl shadow-indigo-500/50">
            <Wallet className="text-white" size={32} />
          </div>
          <h1 className="text-2xl text-white tracking-tight">CampusCross</h1>
        </div>

        {/* Biometric Scanner */}
        <div className="relative mb-12">
          {/* Outer Rings */}
          <div 
            className="absolute inset-0 rounded-full border-2 border-indigo-500/30"
            style={{
              width: '240px',
              height: '240px',
              left: '50%',
              top: '50%',
              transform: `translate(-50%, -50%) scale(${pulseScale})`,
              transition: 'transform 1s ease-in-out'
            }}
          />
          <div 
            className="absolute inset-0 rounded-full border-2 border-purple-500/20"
            style={{
              width: '280px',
              height: '280px',
              left: '50%',
              top: '50%',
              transform: `translate(-50%, -50%) scale(${pulseScale === 1 ? 1.05 : 1})`,
              transition: 'transform 1s ease-in-out'
            }}
          />

          {/* Center Circle */}
          <div className="relative w-48 h-48 rounded-full bg-gradient-to-br from-zinc-900 to-black border-2 border-zinc-800 flex items-center justify-center">
            <div className={`w-32 h-32 rounded-full bg-gradient-to-br from-indigo-500/20 to-purple-500/20 flex items-center justify-center transition-all duration-300 ${
              scanning ? 'scale-110 animate-pulse' : ''
            }`}>
              <Fingerprint 
                className={`transition-all duration-300 ${
                  scanning ? 'text-indigo-400' : 'text-gray-500'
                }`} 
                size={80} 
              />
            </div>
            
            {/* Scanning Effect */}
            {scanning && (
              <div className="absolute inset-0 rounded-full">
                <div className="absolute inset-0 rounded-full bg-gradient-to-b from-indigo-500/50 to-transparent animate-ping" />
              </div>
            )}
          </div>

          {/* Glow Effect */}
          <div className="absolute inset-0 rounded-full bg-gradient-to-br from-indigo-500/30 to-purple-500/30 blur-2xl opacity-50" />
        </div>

        {/* Text */}
        <div className="text-center mb-12">
          <h2 className="text-white text-2xl mb-2 flex items-center justify-center gap-2">
            {scanning ? 'Authenticating...' : 'Welcome Back'}
            {scanning && <Sparkles size={20} className="text-yellow-400 animate-pulse" />}
          </h2>
          <p className="text-gray-400">
            {scanning ? 'Verifying your identity' : 'Use biometric authentication to continue'}
          </p>
        </div>

        {/* Buttons */}
        <div className="w-full max-w-sm space-y-3">
          <Button 
            onClick={handleBiometric}
            disabled={scanning}
            className="w-full bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:from-indigo-600 hover:via-purple-600 hover:to-pink-600 text-white rounded-2xl h-14 shadow-xl shadow-purple-500/30 disabled:opacity-50"
          >
            {scanning ? (
              <span className="flex items-center gap-2">
                <span className="w-2 h-2 bg-white rounded-full animate-bounce" />
                <span className="w-2 h-2 bg-white rounded-full animate-bounce" style={{ animationDelay: '0.1s' }} />
                <span className="w-2 h-2 bg-white rounded-full animate-bounce" style={{ animationDelay: '0.2s' }} />
              </span>
            ) : (
              <>
                <Fingerprint size={20} className="mr-2" />
                Authenticate with Biometric
              </>
            )}
          </Button>

          <Button 
            variant="ghost"
            className="w-full text-gray-400 hover:text-white hover:bg-white/5 rounded-2xl h-12 border border-zinc-800"
          >
            Use Password Instead
          </Button>
        </div>
      </div>

      {/* Footer */}
      <div className="relative z-10 p-6 text-center">
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-zinc-900/50 backdrop-blur-xl border border-zinc-800">
          <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <p className="text-xs text-gray-400">Secured with 256-bit encryption</p>
        </div>
      </div>

      <style>{`
        @keyframes gridMove {
          0% {
            transform: translateY(0);
          }
          100% {
            transform: translateY(60px);
          }
        }
      `}</style>
    </div>
  );
}

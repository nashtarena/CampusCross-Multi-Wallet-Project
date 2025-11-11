import React from 'react';
import { Button } from '../ui/button';
import { ArrowRight, Sparkles } from 'lucide-react';

interface WelcomeProps {
  onNext: () => void;
}

export function Welcome({ onNext }: WelcomeProps) {
  return (
    <div className="min-h-screen bg-black flex flex-col relative overflow-hidden">
      {/* Animated Grid Background */}
      <div className="absolute inset-0">
        <div className="absolute inset-0" style={{
          backgroundImage: `
            linear-gradient(rgba(99, 102, 241, 0.1) 1px, transparent 1px),
            linear-gradient(90deg, rgba(99, 102, 241, 0.1) 1px, transparent 1px)
          `,
          backgroundSize: '50px 50px',
          maskImage: 'radial-gradient(ellipse at center, black 40%, transparent 80%)'
        }} />
      </div>

      {/* Floating Orbs */}
      <div className="absolute top-20 left-10 w-32 h-32 rounded-full bg-gradient-to-br from-indigo-500/30 to-purple-500/30 blur-2xl animate-pulse" style={{ animationDuration: '3s' }} />
      <div className="absolute bottom-40 right-10 w-40 h-40 rounded-full bg-gradient-to-br from-emerald-500/20 to-teal-500/20 blur-2xl animate-pulse" style={{ animationDuration: '4s', animationDelay: '1s' }} />

      {/* Content */}
      <div className="relative z-10 flex-1 flex flex-col px-6">
        {/* Logo Section with Creative Typography */}
        <div className="pt-20 pb-16">
          <div className="relative inline-block">
            <div className="absolute -inset-4 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 rounded-3xl opacity-20 blur-xl" />
            <h1 className="relative text-6xl text-white mb-2 tracking-tighter" style={{ 
              fontFamily: 'system-ui, -apple-system, sans-serif',
              fontWeight: 800,
              letterSpacing: '-0.04em'
            }}>
              Campus
              <span className="block text-5xl bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 bg-clip-text text-transparent mt-1">
                Cross
              </span>
            </h1>
          </div>
          <div className="mt-4 flex items-center gap-2">
            <div className="h-px w-12 bg-gradient-to-r from-indigo-500 to-transparent" />
            <p className="text-sm text-gray-400 tracking-wider uppercase">Multi-Wallet</p>
          </div>
        </div>

        {/* Stacked Feature Cards - Unique Layout */}
        <div className="flex-1 space-y-3 pb-6">
          {/* Card 1 - Tilted */}
          <div className="relative group" style={{ transform: 'rotate(-1deg)' }}>
            <div className="absolute inset-0 bg-gradient-to-r from-emerald-500 to-teal-500 rounded-3xl opacity-20 blur-xl group-hover:opacity-30 transition-opacity" />
            <div className="relative bg-zinc-900/80 backdrop-blur-xl border border-zinc-800 rounded-3xl p-6 group-hover:border-emerald-500/50 transition-all">
              <div className="flex items-center justify-between mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center">
                  <span className="text-xl">💰</span>
                </div>
                <div className="px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-500/30">
                  <p className="text-xs text-emerald-400">5 Currencies</p>
                </div>
              </div>
              <h3 className="text-xl text-white mb-2">Universal Wallets</h3>
              <p className="text-sm text-gray-400 leading-relaxed">
                USD • EUR • GBP • JPY • INR all in one place
              </p>
            </div>
          </div>

          {/* Card 2 - Tilted opposite */}
          <div className="relative group" style={{ transform: 'rotate(1deg)' }}>
            <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-pink-500 rounded-3xl opacity-20 blur-xl group-hover:opacity-30 transition-opacity" />
            <div className="relative bg-zinc-900/80 backdrop-blur-xl border border-zinc-800 rounded-3xl p-6 group-hover:border-purple-500/50 transition-all">
              <div className="flex items-center justify-between mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-purple-400 to-pink-500 flex items-center justify-center">
                  <span className="text-xl">⚡</span>
                </div>
                <div className="px-3 py-1 rounded-full bg-purple-500/20 border border-purple-500/30">
                  <p className="text-xs text-purple-400">Zero Fees</p>
                </div>
              </div>
              <h3 className="text-xl text-white mb-2">Instant Transfers</h3>
              <p className="text-sm text-gray-400 leading-relaxed">
                Send money to anyone, anywhere, instantly
              </p>
            </div>
          </div>

          {/* Card 3 - Straight */}
          <div className="relative group">
            <div className="absolute inset-0 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-3xl opacity-20 blur-xl group-hover:opacity-30 transition-opacity" />
            <div className="relative bg-zinc-900/80 backdrop-blur-xl border border-zinc-800 rounded-3xl p-6 group-hover:border-blue-500/50 transition-all">
              <div className="flex items-center justify-between mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-400 to-cyan-500 flex items-center justify-center">
                  <span className="text-xl">🛡️</span>
                </div>
                <div className="px-3 py-1 rounded-full bg-blue-500/20 border border-blue-500/30">
                  <p className="text-xs text-blue-400">Secured</p>
                </div>
              </div>
              <h3 className="text-xl text-white mb-2">Military-Grade Security</h3>
              <p className="text-sm text-gray-400 leading-relaxed">
                Biometric authentication • 256-bit encryption
              </p>
            </div>
          </div>
        </div>

        {/* Bottom Section */}
        <div className="pb-8 space-y-4">
          {/* Stats Bar */}
          <div className="bg-zinc-900/50 backdrop-blur-xl border border-zinc-800 rounded-2xl p-4">
            <div className="flex items-center justify-around text-center">
              <div>
                <p className="text-lg text-white">500K+</p>
                <p className="text-xs text-gray-500">Students</p>
              </div>
              <div className="w-px h-8 bg-zinc-800" />
              <div>
                <p className="text-lg text-white">150+</p>
                <p className="text-xs text-gray-500">Universities</p>
              </div>
              <div className="w-px h-8 bg-zinc-800" />
              <div>
                <p className="text-lg text-white">$50M</p>
                <p className="text-xs text-gray-500">Volume</p>
              </div>
            </div>
          </div>

          {/* CTA Button */}
          <Button 
            onClick={onNext}
            className="w-full bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:from-indigo-600 hover:via-purple-600 hover:to-pink-600 text-white rounded-2xl h-14 text-base shadow-2xl shadow-purple-500/50 relative overflow-hidden group"
          >
            <span className="relative z-10 flex items-center justify-center gap-2">
              Begin Your Journey
              <ArrowRight size={20} />
            </span>
            <div className="absolute inset-0 bg-gradient-to-r from-pink-500 via-purple-500 to-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity" />
          </Button>

          <button className="w-full text-gray-400 hover:text-white transition-colors text-sm py-3">
            Already have an account? <span className="text-indigo-400">Sign In →</span>
          </button>

          {/* Trust Badge */}
          <div className="flex items-center justify-center gap-2 pt-2">
            <Sparkles size={14} className="text-yellow-500" />
            <p className="text-xs text-gray-500">Licensed & Regulated Financial Service</p>
          </div>
        </div>
      </div>
    </div>
  );
}

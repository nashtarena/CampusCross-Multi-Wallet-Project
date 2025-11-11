import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { ArrowLeft, Mail, Lock, User, Phone, Check } from 'lucide-react';

interface SignUpProps {
  onBack: () => void;
  onNext: () => void;
}

export function SignUp({ onBack, onNext }: SignUpProps) {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  });

  const [focusedField, setFocusedField] = useState<string | null>(null);

  const isFieldFilled = (field: string) => {
    return formData[field as keyof typeof formData].length > 0;
  };

  return (
    <div className="min-h-screen bg-black flex flex-col">
      {/* Unique Header with Progress */}
      <div className="relative">
        <div className="absolute inset-0 bg-gradient-to-b from-zinc-900 to-black" style={{
          clipPath: 'polygon(0 0, 100% 0, 100% calc(100% - 40px), 0 100%)'
        }} />
        
        <div className="relative z-10 p-6 pb-16">
          <div className="flex items-center justify-between mb-8">
            <button 
              onClick={onBack}
              className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center hover:bg-white/10 transition-colors"
            >
              <ArrowLeft size={20} className="text-white" />
            </button>
            
            {/* Progress Dots */}
            <div className="flex gap-2">
              <div className="w-2 h-2 rounded-full bg-gradient-to-r from-indigo-500 to-purple-500" />
              <div className="w-2 h-2 rounded-full bg-zinc-700" />
              <div className="w-2 h-2 rounded-full bg-zinc-700" />
            </div>
          </div>

          <div>
            <h1 className="text-3xl text-white mb-2">Create Account</h1>
            <p className="text-gray-400">Join 500,000+ students worldwide</p>
          </div>
        </div>
      </div>

      {/* Form */}
      <div className="flex-1 px-6 overflow-auto pb-6">
        <div className="space-y-5">
          {/* Full Name */}
          <div className="space-y-2">
            <Label className="text-gray-400 text-sm flex items-center gap-2">
              Full Name
              {isFieldFilled('fullName') && <Check size={14} className="text-emerald-400" />}
            </Label>
            <div className="relative">
              <User 
                className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${
                  focusedField === 'fullName' ? 'text-indigo-400' : 'text-gray-600'
                }`} 
                size={20} 
              />
              <Input
                type="text"
                placeholder="John Doe"
                className={`pl-12 h-14 rounded-2xl bg-zinc-900/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all ${
                  isFieldFilled('fullName') ? 'border-emerald-500/30' : ''
                }`}
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                onFocus={() => setFocusedField('fullName')}
                onBlur={() => setFocusedField(null)}
              />
            </div>
          </div>

          {/* University Email */}
          <div className="space-y-2">
            <Label className="text-gray-400 text-sm flex items-center gap-2">
              University Email
              {isFieldFilled('email') && <Check size={14} className="text-emerald-400" />}
            </Label>
            <div className="relative">
              <Mail 
                className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${
                  focusedField === 'email' ? 'text-indigo-400' : 'text-gray-600'
                }`} 
                size={20} 
              />
              <Input
                type="email"
                placeholder="john@university.edu"
                className={`pl-12 h-14 rounded-2xl bg-zinc-900/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all ${
                  isFieldFilled('email') ? 'border-emerald-500/30' : ''
                }`}
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                onFocus={() => setFocusedField('email')}
                onBlur={() => setFocusedField(null)}
              />
            </div>
          </div>

          {/* Phone Number */}
          <div className="space-y-2">
            <Label className="text-gray-400 text-sm flex items-center gap-2">
              Phone Number
              {isFieldFilled('phone') && <Check size={14} className="text-emerald-400" />}
            </Label>
            <div className="relative">
              <Phone 
                className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${
                  focusedField === 'phone' ? 'text-indigo-400' : 'text-gray-600'
                }`} 
                size={20} 
              />
              <Input
                type="tel"
                placeholder="+1 (555) 000-0000"
                className={`pl-12 h-14 rounded-2xl bg-zinc-900/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all ${
                  isFieldFilled('phone') ? 'border-emerald-500/30' : ''
                }`}
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                onFocus={() => setFocusedField('phone')}
                onBlur={() => setFocusedField(null)}
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-2">
            <Label className="text-gray-400 text-sm flex items-center gap-2">
              Password
              {isFieldFilled('password') && <Check size={14} className="text-emerald-400" />}
            </Label>
            <div className="relative">
              <Lock 
                className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${
                  focusedField === 'password' ? 'text-indigo-400' : 'text-gray-600'
                }`} 
                size={20} 
              />
              <Input
                type="password"
                placeholder="••••••••"
                className={`pl-12 h-14 rounded-2xl bg-zinc-900/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all ${
                  isFieldFilled('password') ? 'border-emerald-500/30' : ''
                }`}
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                onFocus={() => setFocusedField('password')}
                onBlur={() => setFocusedField(null)}
              />
            </div>
          </div>

          {/* Confirm Password */}
          <div className="space-y-2">
            <Label className="text-gray-400 text-sm flex items-center gap-2">
              Confirm Password
              {isFieldFilled('confirmPassword') && <Check size={14} className="text-emerald-400" />}
            </Label>
            <div className="relative">
              <Lock 
                className={`absolute left-4 top-1/2 -translate-y-1/2 transition-colors ${
                  focusedField === 'confirmPassword' ? 'text-indigo-400' : 'text-gray-600'
                }`} 
                size={20} 
              />
              <Input
                type="password"
                placeholder="••••••••"
                className={`pl-12 h-14 rounded-2xl bg-zinc-900/50 border-zinc-800 text-white placeholder:text-gray-600 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-all ${
                  isFieldFilled('confirmPassword') ? 'border-emerald-500/30' : ''
                }`}
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                onFocus={() => setFocusedField('confirmPassword')}
                onBlur={() => setFocusedField(null)}
              />
            </div>
          </div>

          {/* Terms */}
          <div className="bg-zinc-900/50 border border-zinc-800 rounded-2xl p-4">
            <p className="text-xs text-gray-500 leading-relaxed">
              By continuing, you agree to our{' '}
              <button className="text-indigo-400 hover:text-indigo-300">Terms of Service</button>
              {' '}and{' '}
              <button className="text-indigo-400 hover:text-indigo-300">Privacy Policy</button>
            </p>
          </div>
        </div>
      </div>

      {/* Bottom Button */}
      <div className="p-6 border-t border-zinc-900">
        <Button 
          onClick={onNext}
          className="w-full bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 hover:from-indigo-600 hover:via-purple-600 hover:to-pink-600 text-white rounded-2xl h-14 shadow-xl shadow-purple-500/30"
        >
          Continue
        </Button>
      </div>
    </div>
  );
}

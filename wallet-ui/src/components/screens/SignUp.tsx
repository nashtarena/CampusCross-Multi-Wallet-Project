import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Card } from '../ui/card';
import { ArrowLeft, Mail, Lock, User, Phone, UserCheck } from 'lucide-react';
import { useAppContext } from '../../App';
import { authApi, AuthRequest } from '../../services/walletApi';
import { toast } from 'sonner';

interface SignUpProps {
  onBack: () => void;
  onNext: () => void;
}

export function SignUp({ onBack, onNext }: SignUpProps) {
  const { setUserName, setUserPassword } = useAppContext();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    studentId: '',
    password: '',
    confirmPassword: ''
  });

  const handleContinue = async () => {
    // Validation
    if (!formData.fullName || !formData.email || !formData.phone || !formData.studentId || !formData.password || !formData.confirmPassword) {
      toast.error('Please fill in all fields');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      toast.error('Password must be at least 8 characters long');
      return;
    }

    setIsLoading(true);
    
    try {
      const authRequest: AuthRequest = {
        email: formData.email,
        password: formData.password,
        firstName: formData.fullName?.split(' ')[0] || '',
        lastName: formData.fullName?.split(' ').slice(1).join(' ') || '',
        phoneNumber: formData.phone,
        studentId: formData.studentId,
        campusName: '' // Optional for now
      };

      const response = await authApi.register(authRequest);
      
      // Store user data and token
      setUserName(response.fullName);
      setUserPassword(formData.password);
      
      // Store token in localStorage for future API calls
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        email: response.email,
        fullName: response.fullName,
        role: response.role,
        status: response.status,
        kycStatus: response.kycStatus || 'NOT_STARTED'
      }));
      
      toast.success('Account created successfully!');
      onNext();
    } catch (error) {
      console.error('Registration error:', error);
      toast.error(error instanceof Error ? error.message : 'Registration failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <div className="p-6 flex items-center gap-3">
        <button onClick={onBack} className="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center">
          <ArrowLeft size={20} className="text-gray-700" />
        </button>
        <div>
          <h1 className="text-xl text-gray-900">Create Account</h1>
          <p className="text-sm text-gray-600">Join CampusCross today</p>
        </div>
      </div>

      {/* Form */}
      <div className="flex-1 px-6 overflow-auto">
        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="fullName" className="text-gray-700">Full Name</Label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="fullName"
                  type="text"
                  placeholder=""
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-gray-700">University Email</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="email"
                  type="email"
                  placeholder=""
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone" className="text-gray-700">Phone Number</Label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="phone"
                  type="tel"
                  placeholder=""
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="studentId" className="text-gray-700">Student ID</Label>
              <div className="relative">
                <UserCheck className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="studentId"
                  type="text"
                  placeholder="Enter your student ID"
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.studentId}
                  onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-gray-700">Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword" className="text-gray-700">Confirm Password</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.confirmPassword}
                  onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                />
              </div>
            </div>

            <div className="pt-2">
              <p className="text-xs text-gray-600">
                By continuing, you agree to our Terms of Service and Privacy Policy
              </p>
            </div>
          </div>
        </Card>
      </div>

      {/* Bottom Button */}
      <div className="p-6">
        <Button 
          onClick={handleContinue}
          disabled={isLoading}
          className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white rounded-xl h-12"
        >
          {isLoading ? 'Creating Account...' : 'Continue'}
        </Button>
      </div>
    </div>
  );
}
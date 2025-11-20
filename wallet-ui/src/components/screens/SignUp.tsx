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
  onAdminSuccess?: () => void;
}

export function SignUp({ onBack, onNext, onAdminSuccess }: SignUpProps) {
  const { setUserName, setUserPassword, setUserId } = useAppContext();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    studentId: '',
    password: '',
    confirmPassword: '',
    role: 'STUDENT',
    campusName: '',
    country: ''
  });

  const handleContinue = async () => {
    // Validation
    const requiredFields = [formData.fullName, formData.email, formData.phone, formData.studentId, formData.password, formData.confirmPassword, formData.campusName];

    if (requiredFields.some(field => !field)) {
      toast.error('Please fill in all fields');
      return;
    }

    if (formData.role === 'MERCHANT' && !formData.country) {
      toast.error('Please enter your country');
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
        campusName: formData.campusName || undefined,
        role: formData.role,
        country: formData.role === 'MERCHANT' ? formData.country : undefined
      };

      const response = await authApi.register(authRequest);

      // Store user data and token
      setUserName(response.fullName);
      setUserPassword(formData.password);
      setUserId(parseInt(response.userId));

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

      // If admin, bypass KYC and go directly to home
      if (response.role === 'ADMIN') {
        // Admins don't need KYC, go directly to home
        if (onAdminSuccess) {
          onAdminSuccess();
        } else {
          // Fallback if callback not provided
          onNext();
        }
      } else {
        // Students go through KYC process
        onNext();
      }
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
              <Label htmlFor="role" className="text-gray-700">Account Type</Label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <select
                  id="role"
                  className="w-full h-12 rounded-xl border border-gray-200 pl-10 pr-3 text-gray-700 bg-gray-50 appearance-none cursor-pointer"
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                >
                  <option value="STUDENT">Student</option>
                  <option value="ADMIN">Administrator</option>
                  <option value="MERCHANT">Campus Merchant</option>
                </select>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="campusName" className="text-gray-700">College/University Name *</Label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="campusName"
                  type="text"
                  placeholder="Enter your institution name"
                  className="pl-10 h-12 rounded-xl border-gray-200"
                  value={formData.campusName}
                  onChange={(e) => setFormData({ ...formData, campusName: e.target.value })}
                  required
                />
              </div>
            </div>

            {formData.role === 'MERCHANT' && (
              <div className="space-y-2">
                <Label htmlFor="country" className="text-gray-700">Business Country</Label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                  <Input
                    id="country"
                    type="text"
                    placeholder="Enter your country"
                    className="pl-10 h-12 rounded-xl border-gray-200"
                    value={formData.country}
                    onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                  />
                </div>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="studentId" className="text-gray-700">
                {formData.role === 'ADMIN'
                  ? 'Admin ID'
                  : formData.role === 'STUDENT'
                    ? 'Student ID'
                    : formData.role === 'MERCHANT'
                      ? 'Merchant ID'
                      : ''}
              </Label>
              <div className="relative">
                <UserCheck className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                <Input
                  id="studentId"
                  type="text"
                  placeholder={formData.role === 'ADMIN'
                    ? 'Enter Your Admin ID'
                    : formData.role === 'STUDENT'
                      ? 'Enter Your Student ID'
                      : formData.role === 'MERCHANT'
                        ? 'Enter Your Merchant ID'
                        : ''}
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
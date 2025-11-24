import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Card } from '../ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { ArrowLeft, Shield, CheckCircle2, Loader2 } from 'lucide-react';

interface KYCTier1Props {
  userId: string | number; // ADDED: Need userId from auth/session
  onBack: () => void;
  onNext: (accessToken: string, applicantId: string) => void; // CHANGED: Pass token to next step
}

export function KYCTier1({ userId, onBack, onNext }: KYCTier1Props) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // UPDATED: Added all required fields from backend
  const [formData, setFormData] = useState({
    userId: userId,
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    dateOfBirth: '',
    countryOfResidence: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    stateProvince: '',
    postalCode: ''
  });

  // ADDED: Form validation
  const isFormValid = () => {
    return (
      formData.firstName.trim() !== '' &&
      formData.lastName.trim() !== '' &&
      formData.email.trim() !== '' &&
      formData.phoneNumber.trim() !== '' &&
      formData.dateOfBirth !== '' &&
      formData.countryOfResidence !== '' &&
      formData.addressLine1.trim() !== '' &&
      formData.city.trim() !== '' &&
      formData.postalCode.trim() !== ''
    );
  };

  // ADDED: Submit to backend
  const handleSubmit = async () => {
    if (!isFormValid()) {
      setError('Please fill in all required fields');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      // Call your backend API
      const response = await fetch('https://campuscross-multi-wallet-latest.onrender.com/api/v1/kyc/tier1', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          // Add your auth token if needed
          // 'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify(formData)
      });

      const result = await response.json();

      if (result.success && result.sumsubAccessToken) {
        console.log('Tier 1 complete! Access token received:', result.sumsubAccessToken);
        
        // Pass the access token to the next step (Sumsub SDK)
        onNext(result.sumsubAccessToken, result.sumsubApplicantId);
      } else {
        setError(result.message || 'Failed to submit KYC information');
      }
    } catch (err) {
      console.error('KYC submission error:', err);
      setError('Network error. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <div className="p-6 flex items-center gap-3">
        <button 
          onClick={onBack} 
          disabled={isLoading}
          className="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center disabled:opacity-50"
        >
          <ArrowLeft size={20} className="text-gray-700" />
        </button>
        <div>
          <h1 className="text-xl text-gray-900">KYC Verification - Tier 1</h1>
          <p className="text-sm text-gray-600">Basic Information</p>
        </div>
      </div>

      {/* Progress */}
      <div className="px-6 mb-4">
        <div className="flex items-center gap-2">
          <div className="flex-1 h-2 bg-indigo-600 rounded-full" />
          <div className="flex-1 h-2 bg-gray-200 rounded-full" />
          <div className="flex-1 h-2 bg-gray-200 rounded-full" />
        </div>
        <p className="text-xs text-gray-600 mt-2">Step 1 of 3</p>
      </div>

      {/* Info Card */}
      <div className="px-6 mb-4">
        <Card className="p-4 bg-indigo-50 border-indigo-200">
          <div className="flex gap-3">
            <Shield className="text-indigo-600 flex-shrink-0" size={20} />
            <div>
              <p className="text-sm text-indigo-900 mb-1">Tier 1 Benefits</p>
              <ul className="text-xs text-indigo-700 space-y-1">
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>Basic profile complete</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>Ready for document verification</span>
                </li>
              </ul>
            </div>
          </div>
        </Card>
      </div>

      {/* Error Message */}
      {error && (
        <div className="px-6 mb-4">
          <Card className="p-4 bg-red-50 border-red-200">
            <p className="text-sm text-red-700">{error}</p>
          </Card>
        </div>
      )}

      {/* Form */}
      <div className="flex-1 px-6 overflow-auto">
        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="space-y-4">
            {/* Personal Information */}
            <div className="space-y-4">
              <h3 className="text-sm font-medium text-gray-900">Personal Information</h3>
              
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName" className="text-gray-700">
                    First Name <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="firstName"
                    type="text"
                    placeholder="John"
                    className="h-12 rounded-xl border-gray-200"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="lastName" className="text-gray-700">
                    Last Name <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="lastName"
                    type="text"
                    placeholder="Doe"
                    className="h-12 rounded-xl border-gray-200"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="email" className="text-gray-700">
                  Email Address <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="john.doe@university.edu"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="text-gray-700">
                  Phone Number <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="phoneNumber"
                  type="tel"
                  placeholder="+1234567890"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="dateOfBirth" className="text-gray-700">
                  Date of Birth <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="dateOfBirth"
                  type="date"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.dateOfBirth}
                  onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                  disabled={isLoading}
                />
              </div>
            </div>

            {/* Address Information */}
            <div className="space-y-4 pt-4 border-t border-gray-200">
              <h3 className="text-sm font-medium text-gray-900">Address Information</h3>

              <div className="space-y-2">
                <Label htmlFor="countryOfResidence" className="text-gray-700">
                  Country of Residence <span className="text-red-500">*</span>
                </Label>
                <Select 
                  value={formData.countryOfResidence} 
                  onValueChange={(value) => setFormData({ ...formData, countryOfResidence: value })}
                  disabled={isLoading}
                >
                  <SelectTrigger className="h-12 rounded-xl border-gray-200">
                    <SelectValue placeholder="Select country" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="US">United States</SelectItem>
                    <SelectItem value="GB">United Kingdom</SelectItem>
                    <SelectItem value="DE">Germany</SelectItem>
                    <SelectItem value="FR">France</SelectItem>
                    <SelectItem value="JP">Japan</SelectItem>
                    <SelectItem value="IN">India</SelectItem>
                    <SelectItem value="AU">Australia</SelectItem>
                    <SelectItem value="CA">Canada</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="addressLine1" className="text-gray-700">
                  Address Line 1 <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="addressLine1"
                  type="text"
                  placeholder="123 Main Street"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.addressLine1}
                  onChange={(e) => setFormData({ ...formData, addressLine1: e.target.value })}
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="addressLine2" className="text-gray-700">
                  Address Line 2 (Optional)
                </Label>
                <Input
                  id="addressLine2"
                  type="text"
                  placeholder="Apt 4B"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.addressLine2}
                  onChange={(e) => setFormData({ ...formData, addressLine2: e.target.value })}
                  disabled={isLoading}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="city" className="text-gray-700">
                    City <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="city"
                    type="text"
                    placeholder="New York"
                    className="h-12 rounded-xl border-gray-200"
                    value={formData.city}
                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="stateProvince" className="text-gray-700">
                    State/Province
                  </Label>
                  <Input
                    id="stateProvince"
                    type="text"
                    placeholder="NY"
                    className="h-12 rounded-xl border-gray-200"
                    value={formData.stateProvince}
                    onChange={(e) => setFormData({ ...formData, stateProvince: e.target.value })}
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="postalCode" className="text-gray-700">
                  Postal/ZIP Code <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="postalCode"
                  type="text"
                  placeholder="10001"
                  className="h-12 rounded-xl border-gray-200"
                  value={formData.postalCode}
                  onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                  disabled={isLoading}
                />
              </div>
            </div>
          </div>
        </Card>
      </div>

      {/* Bottom Button */}
      <div className="p-6">
        <Button 
          onClick={handleSubmit}
          disabled={!isFormValid() || isLoading}
          className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white rounded-xl h-12 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? (
            <>
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              Processing...
            </>
          ) : (
            'Continue to Document Verification'
          )}
        </Button>
        <p className="text-xs text-center text-gray-500 mt-2">
          Your information is encrypted and secure
        </p>
      </div>
    </div>
  );
}
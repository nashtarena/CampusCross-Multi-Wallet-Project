import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Card } from '../ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { ArrowLeft, Shield, CheckCircle2 } from 'lucide-react';

interface KYCTier1Props {
  onBack: () => void;
  onNext: () => void;
}

export function KYCTier1({ onBack, onNext }: KYCTier1Props) {
  const [formData, setFormData] = useState({
    studentId: '',
    university: '',
    country: '',
    dateOfBirth: ''
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <div className="p-6 flex items-center gap-3">
        <button onClick={onBack} className="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center">
          <ArrowLeft size={20} className="text-gray-700" />
        </button>
        <div>
          <h1 className="text-xl text-gray-900">KYC Verification - Tier 1</h1>
          <p className="text-sm text-gray-600">Basic verification</p>
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
                  <span>Daily limit: $500</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>P2P transfers enabled</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>Campus payments</span>
                </li>
              </ul>
            </div>
          </div>
        </Card>
      </div>

      {/* Form */}
      <div className="flex-1 px-6 overflow-auto">
        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="studentId" className="text-gray-700">Student ID</Label>
              <Input
                id="studentId"
                type="text"
                placeholder="STU123456"
                className="h-12 rounded-xl border-gray-200"
                value={formData.studentId}
                onChange={(e) => setFormData({ ...formData, studentId: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="university" className="text-gray-700">University Name</Label>
              <Input
                id="university"
                type="text"
                placeholder="Harvard University"
                className="h-12 rounded-xl border-gray-200"
                value={formData.university}
                onChange={(e) => setFormData({ ...formData, university: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="country" className="text-gray-700">Country of Residence</Label>
              <Select value={formData.country} onValueChange={(value) => setFormData({ ...formData, country: value })}>
                <SelectTrigger className="h-12 rounded-xl border-gray-200">
                  <SelectValue placeholder="Select country" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="us">United States</SelectItem>
                  <SelectItem value="uk">United Kingdom</SelectItem>
                  <SelectItem value="eu">European Union</SelectItem>
                  <SelectItem value="jp">Japan</SelectItem>
                  <SelectItem value="in">India</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="dateOfBirth" className="text-gray-700">Date of Birth</Label>
              <Input
                id="dateOfBirth"
                type="date"
                className="h-12 rounded-xl border-gray-200"
                value={formData.dateOfBirth}
                onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
              />
            </div>
          </div>
        </Card>
      </div>

      {/* Bottom Button */}
      <div className="p-6">
        <Button 
          onClick={onNext}
          className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white rounded-xl h-12"
        >
          Continue to Tier 2
        </Button>
      </div>
    </div>
  );
}

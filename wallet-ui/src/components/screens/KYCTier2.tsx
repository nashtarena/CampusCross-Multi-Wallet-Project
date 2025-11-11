import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { ArrowLeft, Upload, FileText, CheckCircle2, Camera } from 'lucide-react';

interface KYCTier2Props {
  onBack: () => void;
  onNext: () => void;
}

export function KYCTier2({ onBack, onNext }: KYCTier2Props) {
  const [uploads, setUploads] = useState({
    idDocument: false,
    proofOfAddress: false,
    selfie: false
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <div className="p-6 flex items-center gap-3">
        <button onClick={onBack} className="w-10 h-10 rounded-full bg-white shadow-sm flex items-center justify-center">
          <ArrowLeft size={20} className="text-gray-700" />
        </button>
        <div>
          <h1 className="text-xl text-gray-900">KYC Verification - Tier 2</h1>
          <p className="text-sm text-gray-600">Document verification</p>
        </div>
      </div>

      {/* Progress */}
      <div className="px-6 mb-4">
        <div className="flex items-center gap-2">
          <div className="flex-1 h-2 bg-indigo-600 rounded-full" />
          <div className="flex-1 h-2 bg-indigo-600 rounded-full" />
          <div className="flex-1 h-2 bg-gray-200 rounded-full" />
        </div>
        <p className="text-xs text-gray-600 mt-2">Step 2 of 3</p>
      </div>

      {/* Info Card */}
      <div className="px-6 mb-4">
        <Card className="p-4 bg-purple-50 border-purple-200">
          <div className="flex gap-3">
            <FileText className="text-purple-600 flex-shrink-0" size={20} />
            <div>
              <p className="text-sm text-purple-900 mb-1">Tier 2 Benefits</p>
              <ul className="text-xs text-purple-700 space-y-1">
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>Daily limit: $5,000</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>International remittance</span>
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 size={12} />
                  <span>Priority support</span>
                </li>
              </ul>
            </div>
          </div>
        </Card>
      </div>

      {/* Upload Cards */}
      <div className="flex-1 px-6 overflow-auto space-y-4">
        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center">
                <FileText className="text-indigo-600" size={20} />
              </div>
              <div>
                <p className="text-gray-900">ID Document</p>
                <p className="text-xs text-gray-600">Passport or Driver's License</p>
              </div>
            </div>
            {uploads.idDocument && <CheckCircle2 className="text-green-500" size={20} />}
          </div>
          <Button 
            variant="outline" 
            className="w-full rounded-xl h-11 border-dashed"
            onClick={() => setUploads({ ...uploads, idDocument: true })}
          >
            <Upload size={16} className="mr-2" />
            {uploads.idDocument ? 'Uploaded' : 'Upload Document'}
          </Button>
        </Card>

        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center">
                <FileText className="text-purple-600" size={20} />
              </div>
              <div>
                <p className="text-gray-900">Proof of Address</p>
                <p className="text-xs text-gray-600">Utility bill or Bank statement</p>
              </div>
            </div>
            {uploads.proofOfAddress && <CheckCircle2 className="text-green-500" size={20} />}
          </div>
          <Button 
            variant="outline" 
            className="w-full rounded-xl h-11 border-dashed"
            onClick={() => setUploads({ ...uploads, proofOfAddress: true })}
          >
            <Upload size={16} className="mr-2" />
            {uploads.proofOfAddress ? 'Uploaded' : 'Upload Document'}
          </Button>
        </Card>

        <Card className="p-6 bg-white shadow-lg border-0">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                <Camera className="text-blue-600" size={20} />
              </div>
              <div>
                <p className="text-gray-900">Selfie Verification</p>
                <p className="text-xs text-gray-600">Photo holding your ID</p>
              </div>
            </div>
            {uploads.selfie && <CheckCircle2 className="text-green-500" size={20} />}
          </div>
          <Button 
            variant="outline" 
            className="w-full rounded-xl h-11 border-dashed"
            onClick={() => setUploads({ ...uploads, selfie: true })}
          >
            <Camera size={16} className="mr-2" />
            {uploads.selfie ? 'Captured' : 'Take Selfie'}
          </Button>
        </Card>
      </div>

      {/* Bottom Button */}
      <div className="p-6">
        <Button 
          onClick={onNext}
          className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white rounded-xl h-12"
        >
          Submit for Review
        </Button>
      </div>
    </div>
  );
}

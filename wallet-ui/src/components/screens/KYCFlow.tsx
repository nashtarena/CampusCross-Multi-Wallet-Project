import React, { useState } from 'react';
import { KYCTier1 } from './KYCTier1';
import { KYCTier2 } from './KYCTier2';
import { KYCStatusPolling } from './KYCStatusPolling';

interface KYCFlowProps {
  userId: string;
  onComplete: () => void;
}

export function KYCFlow({ userId, onComplete }: KYCFlowProps) {
  const [step, setStep] = useState<'tier1' | 'tier2' | 'polling'>('tier1');
  const [accessToken, setAccessToken] = useState('');

  // Get user role from localStorage
  const getUserRole = () => {
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        return user.role || 'STUDENT';
      }
    } catch (error) {
      console.error('Failed to get user role:', error);
    }
    return 'STUDENT';
  };

  const handleKYCComplete = () => {
    const userRole = getUserRole();
    
    // Update KYC status in localStorage
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        user.kycStatus = 'VERIFIED';
        localStorage.setItem('user', JSON.stringify(user));
      }
    } catch (error) {
      console.error('Failed to update KYC status:', error);
    }

    // Call the onComplete callback which should handle navigation
    onComplete();
  };

  return (
    <>
      {step === 'tier1' && (
        <KYCTier1
          userId={userId}
          onBack={() => {}}
          onNext={(token) => {
            setAccessToken(token);
            setStep('tier2');
          }}
        />
      )}

      {step === 'tier2' && (
        <KYCTier2
          accessToken={accessToken}
          onComplete={() => setStep('polling')}
          onError={(err) => alert(err)}
        />
      )}

      {step === 'polling' && (
        <KYCStatusPolling
          userId={userId}
          onVerified={handleKYCComplete}
          onRejected={(reason) => alert(reason)}
        />
      )}
    </>
  );
}
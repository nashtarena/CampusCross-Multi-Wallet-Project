import React, { useState } from 'react';
import { KYCTier1 } from './KYCTier1';
import { KYCTier2 } from './KYCTier2';
import { KYCStatusPolling } from './KYCStatusPolling';

export function KYCFlow({ userId, onComplete }: any) {
  const [step, setStep] = useState<'tier1' | 'tier2' | 'polling'>('tier1');
  const [accessToken, setAccessToken] = useState('');

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
          onVerified={onComplete}
          onRejected={(reason) => alert(reason)}
        />
      )}
    </>
  );
}
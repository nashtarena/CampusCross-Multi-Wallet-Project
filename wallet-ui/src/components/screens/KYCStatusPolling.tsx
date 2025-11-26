import React, { useEffect, useState } from 'react';
import { Loader2, CheckCircle2, XCircle } from 'lucide-react';

interface KYCStatusPollingProps {
  userId: string | number;
  onVerified: () => void;
  onRejected: (reason: string) => void;
}

const baseUrl = import.meta.env.VITE_API_BASE_URL;

export function KYCStatusPolling({ userId, onVerified, onRejected }: KYCStatusPollingProps) {
  const [status, setStatus] = useState<string>('PENDING');
  const [message, setMessage] = useState<string>('Processing your documents...');

  useEffect(() => {
    const pollInterval = setInterval(async () => {
      try {
        const response = await fetch(`${baseUrl}/api/v1/kyc/status/${userId}`);
        const data = await response.json();

        setStatus(data.status);
        setMessage(data.message);

        // Check if Tier 2 or Tier 3 approved
        if ((data.currentTier === 'TIER_2' || data.currentTier === 'TIER_3') && 
            data.status === 'APPROVED') {
          clearInterval(pollInterval);
          onVerified();
        }

        // Check if rejected
        if (data.status === 'REJECTED') {
          clearInterval(pollInterval);
          onRejected(data.message);
        }

      } catch (error) {
        console.error('Status check error:', error);
      }
    }, 5000); // Poll every 5 seconds

    // Cleanup
    return () => clearInterval(pollInterval);
  }, [userId]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 text-center">
        {status === 'PENDING' || status === 'UNDER_REVIEW' ? (
          <>
            <Loader2 className="animate-spin mx-auto mb-6 text-indigo-600" size={64} />
            <h2 className="text-2xl font-bold mb-2">Verifying Your Identity</h2>
            <p className="text-gray-600 mb-4">{message}</p>
            <p className="text-sm text-gray-500">This usually takes 1-2 minutes</p>
          </>
        ) : status === 'APPROVED' ? (
          <>
            <CheckCircle2 className="mx-auto mb-6 text-green-600" size={64} />
            <h2 className="text-2xl font-bold mb-2">Verification Complete!</h2>
            <p className="text-gray-600">You can now send money</p>
          </>
        ) : (
          <>
            <XCircle className="mx-auto mb-6 text-red-600" size={64} />
            <h2 className="text-2xl font-bold mb-2">Verification Failed</h2>
            <p className="text-gray-600">{message}</p>
          </>
        )}
      </div>
    </div>
  );
}
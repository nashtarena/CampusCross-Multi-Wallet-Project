import React, { useEffect, useRef } from 'react';
import snsWebSdk from '@sumsub/websdk';
import { Loader2 } from 'lucide-react';

interface KYCTier2Props {
  accessToken: string;
  onComplete: () => void;
  onError: (error: string) => void;
}

/**
 * KYC Tier 2 - Document Verification
 * Uses Sumsub Web SDK
 * Works on: Desktop browsers, Mobile browsers (iOS Safari, Chrome, etc.)
 */
export function KYCTier2({ accessToken, onComplete, onError }: KYCTier2Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const sdkInstanceRef = useRef<any>(null);

  useEffect(() => {
    if (!containerRef.current) {
      console.error('Container ref is null');
      return;
    }

    console.log('Initializing Sumsub Web SDK...');
    console.log('Access token:', accessToken.substring(0, 30) + '...');

    try {
      const sdk = snsWebSdk
        .init(
          accessToken,
          // Token refresh function (called if token expires)
          () => {
            console.log('Token refresh requested');
            return Promise.resolve(accessToken);
          }
        )
        .withConf({
          lang: 'en',
          theme: 'light',
          // Mobile-optimized configuration
          uiConf: {
            customCssStr: `
              :root {
                --primary-color: #6366f1;
                --secondary-color: #8b5cf6;
              }
              /* Mobile-friendly adjustments */
              @media (max-width: 768px) {
                .step-content {
                  padding: 16px !important;
                }
              }
            `
          }
        })
        // Event: When user completes document upload
        .on('idCheck.onApplicantSubmitted', (payload) => {
          console.log('✅ Documents submitted successfully:', payload);
          onComplete();
        })
        // Event: When there's an error
        .on('idCheck.onError', (error) => {
          console.error('❌ Sumsub error:', error);
          onError('Document upload failed. Please try again.');
        })
        // Event: When applicant is loaded
        .on('idCheck.onApplicantLoaded', (payload) => {
          console.log('Applicant loaded:', payload);
        })
        // Event: When a step is completed
        .on('idCheck.onStepCompleted', (payload) => {
          console.log('Step completed:', payload);
        })
        .build();

      // Launch the SDK
      sdk.launch(containerRef.current);
      sdkInstanceRef.current = sdk;

      console.log('✅ Sumsub SDK launched successfully');

    } catch (error) {
      console.error('Failed to initialize Sumsub SDK:', error);
      onError('Failed to launch verification. Please try again.');
    }

    // Cleanup function
    return () => {
      if (sdkInstanceRef.current) {
        try {
          sdkInstanceRef.current.destroy();
          console.log('SDK destroyed');
        } catch (error) {
          console.error('Error destroying SDK:', error);
        }
      }
    };
  }, [accessToken, onComplete, onError]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50 flex flex-col">
      {/* Header */}
      <div className="p-6">
        <div>
          <h1 className="text-xl text-gray-900">Document Verification</h1>
          <p className="text-sm text-gray-600">Upload your identity documents</p>
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

      {/* SDK Container */}
      <div className="flex-1 px-6 pb-6 overflow-auto">
        <div 
          ref={containerRef}
          className="bg-white rounded-lg shadow-lg w-full mx-auto"
          style={{ 
            minHeight: '600px',
            maxWidth: '900px' // Looks good on desktop
          }}
        >
          {/* Loading indicator - shows briefly while SDK initializes */}
          <div className="flex flex-col items-center justify-center h-full p-8">
            <Loader2 className="animate-spin text-indigo-600 mb-4" size={40} />
            <p className="text-gray-600 text-center">Loading verification...</p>
            <p className="text-sm text-gray-500 text-center mt-2">
              This may take a few seconds
            </p>
          </div>
        </div>
      </div>

      {/* Mobile hint */}
      <div className="px-6 pb-4">
        <p className="text-xs text-gray-500 text-center">
          📱 On mobile? You can use your camera to scan documents
        </p>
      </div>
    </div>
  );
}

export default KYCTier2;
import React, { useState, createContext, useContext } from 'react';
import { Welcome } from './components/screens/Welcome';
import { SignUp } from './components/screens/SignUp';
import { KYCTier1 } from './components/screens/KYCTier1';
import { KYCTier2 } from './components/screens/KYCTier2';
import { KYCStatusPolling } from './components/screens/KYCStatusPolling'; // NEW - You need to create this
import { BiometricLogin } from './components/screens/BiometricLogin';
import { Home } from './components/screens/Home';
import { CurrencyConversion } from './components/screens/CurrencyConversion';
import { P2PTransfer } from './components/screens/P2PTransfer';
import { CampusPayments } from './components/screens/CampusPayments';
import { Remittance } from './components/screens/Remittance';
import { AdminPanel } from './components/screens/AdminPanel';
import { RateAlerts } from './components/screens/RateAlerts';
import { Analytics } from './components/screens/Analytics';
import { Toaster } from './components/ui/sonner';

type Screen = 
  | 'welcome' 
  | 'signup' 
  | 'kyc1' 
  | 'kyc2' 
  | 'kyc-polling' // NEW - Added polling screen
  | 'biometric' 
  | 'home' 
  | 'conversion' 
  | 'p2p' 
  | 'campus' 
  | 'remittance' 
  | 'admin' 
  | 'alerts' 
  | 'analytics'
  | 'settings';

interface AppContextType {
  userName: string;
  setUserName: (name: string) => void;
  userPassword: string;
  setUserPassword: (password: string) => void;
  userId: number;
  setUserId: (id: number) => void;
  theme: 'light' | 'dark';
  toggleTheme: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext must be used within AppProvider');
  }
  return context;
};

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('welcome');
  const [userName, setUserName] = useState('');
  const [userPassword, setUserPassword] = useState('');
  const [userId, setUserId] = useState(9002); // Store userId in state
  const [accessToken, setAccessToken] = useState(''); // Store Sumsub access token
  const [theme, setTheme] = useState<'light' | 'dark'>('light');

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  const renderScreen = () => {
    switch (currentScreen) {
      case 'welcome':
        return <Welcome onNext={() => setCurrentScreen('signup')} />;
      
      case 'signup':
        return (
          <SignUp 
            onBack={() => setCurrentScreen('welcome')}
            onNext={() => setCurrentScreen('kyc1')}
          />
        );
      
      // UPDATED: KYC Tier 1 - Now receives access token
      case 'kyc1':
        return (
          <KYCTier1 
            userId={userId}
            onBack={() => setCurrentScreen('signup')}
            onNext={(token, applicantId) => {
              // Save the access token from backend
              setAccessToken(token);
              console.log('Received access token:', token.substring(0, 30) + '...');
              console.log('Applicant ID:', applicantId);
              // Move to Tier 2 (Sumsub SDK)
              setCurrentScreen('kyc2');
            }}
          />
        );
      
      // UPDATED: KYC Tier 2 - Launches Sumsub SDK
      case 'kyc2':
        return (
          <KYCTier2 
            accessToken={accessToken}
            onComplete={() => {
              console.log('Documents uploaded to Sumsub');
              // Move to polling screen (wait for webhook)
              setCurrentScreen('kyc-polling');
            }}
            onError={(error) => {
              console.error('KYC Tier 2 error:', error);
              alert(error);
              // Stay on same screen or go back
            }}
          />
        );
      
      // NEW: KYC Status Polling - Waits for backend webhook
      case 'kyc-polling':
        return (
          <KYCStatusPolling 
            userId={userId}
            onVerified={() => {
              console.log('✅ KYC Verified! User is Tier 2+');
              // KYC complete, move to biometric setup
              setCurrentScreen('biometric');
            }}
            onRejected={(reason) => {
              console.error('❌ KYC Rejected:', reason);
              alert(`Verification failed: ${reason}`);
              // Go back to Tier 1 to retry
              setCurrentScreen('kyc1');
            }}
          />
        );
      
      case 'biometric':
        return <BiometricLogin onNext={() => setCurrentScreen('home')} />;
      
      case 'home':
        return <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;
      
      case 'conversion':
        return <CurrencyConversion onBack={() => setCurrentScreen('home')} />;
      
      case 'p2p':
        return <P2PTransfer onBack={() => setCurrentScreen('home')} />;
      
      case 'campus':
        return <CampusPayments onBack={() => setCurrentScreen('home')} />;
      
      case 'remittance':
        return <Remittance onBack={() => setCurrentScreen('home')} />;
      
      case 'admin':
        return <AdminPanel onBack={() => setCurrentScreen('home')} />;
      
      case 'alerts':
        return <RateAlerts onBack={() => setCurrentScreen('home')} userId={userId} />;
      
      case 'analytics':
        return <Analytics onBack={() => setCurrentScreen('home')} />;
      
      case 'settings':
        return <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;
      
      default:
        return <Welcome onNext={() => setCurrentScreen('signup')} />;
    }
  };

  return (
    <AppContext.Provider value={{ 
      userName, 
      setUserName, 
      userPassword, 
      setUserPassword, 
      userId,
      setUserId,
      theme, 
      toggleTheme 
    }}>
      <div className="max-w-md mx-auto bg-white min-h-screen shadow-2xl">
        {renderScreen()}
        <Toaster position="top-center" />
        
        {/* Development Navigation Helper */}
        {process.env.NODE_ENV === 'development' && (
          <div className="fixed bottom-4 right-4 bg-black/80 backdrop-blur-sm text-white p-4 rounded-xl shadow-lg max-w-xs z-50">
            <p className="text-xs mb-2 font-semibold">Dev Navigation</p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button onClick={() => setCurrentScreen('welcome')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Welcome</button>
              <button onClick={() => setCurrentScreen('signup')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">SignUp</button>
              <button onClick={() => setCurrentScreen('kyc1')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">KYC 1</button>
              <button onClick={() => setCurrentScreen('kyc2')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">KYC 2</button>
              <button onClick={() => setCurrentScreen('kyc-polling')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Polling</button>
              <button onClick={() => setCurrentScreen('biometric')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Biometric</button>
              <button onClick={() => setCurrentScreen('home')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Home</button>
              <button onClick={() => setCurrentScreen('conversion')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Convert</button>
              <button onClick={() => setCurrentScreen('p2p')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">P2P</button>
              <button onClick={() => setCurrentScreen('campus')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Campus</button>
              <button onClick={() => setCurrentScreen('remittance')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Remittance</button>
              <button onClick={() => setCurrentScreen('admin')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Admin</button>
              <button onClick={() => setCurrentScreen('alerts')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Alerts</button>
              <button onClick={() => setCurrentScreen('analytics')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Analytics</button>
            </div>
            <div className="mt-2 pt-2 border-t border-white/20">
              <p className="text-xs opacity-60">Current: {currentScreen}</p>
              <p className="text-xs opacity-60">User ID: {userId}</p>
            </div>
          </div>
        )}
      </div>
    </AppContext.Provider>
  );
}
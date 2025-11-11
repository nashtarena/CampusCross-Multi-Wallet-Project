import React, { useState } from 'react';
import { Welcome } from './components/screens/Welcome';
import { SignUp } from './components/screens/SignUp';
import { KYCTier1 } from './components/screens/KYCTier1';
import { KYCTier2 } from './components/screens/KYCTier2';
import { BiometricLogin } from './components/screens/BiometricLogin';
import { Home } from './components/screens/Home';
import { CurrencyConversion } from './components/screens/CurrencyConversion';
import { P2PTransfer } from './components/screens/P2PTransfer';
import { CampusPayments } from './components/screens/CampusPayments';
import { Remittance } from './components/screens/Remittance';
import { AdminPanel } from './components/screens/AdminPanel';
import { RateAlerts } from './components/screens/RateAlerts';
import { Analytics } from './components/screens/Analytics';

type Screen = 
  | 'welcome' 
  | 'signup' 
  | 'kyc1' 
  | 'kyc2' 
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

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('welcome');

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
      case 'kyc1':
        return (
          <KYCTier1 
            onBack={() => setCurrentScreen('signup')}
            onNext={() => setCurrentScreen('kyc2')}
          />
        );
      case 'kyc2':
        return (
          <KYCTier2 
            onBack={() => setCurrentScreen('kyc1')}
            onNext={() => setCurrentScreen('biometric')}
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
        return <RateAlerts onBack={() => setCurrentScreen('home')} />;
      case 'analytics':
        return <Analytics onBack={() => setCurrentScreen('home')} />;
      case 'settings':
        return <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;
      default:
        return <Welcome onNext={() => setCurrentScreen('signup')} />;
    }
  };

  return (
    <div className="max-w-md mx-auto bg-white min-h-screen shadow-2xl">
      {renderScreen()}
      
      {/* Development Navigation Helper */}
      {process.env.NODE_ENV === 'development' && (
        <div className="fixed bottom-4 right-4 bg-black/80 backdrop-blur-sm text-white p-4 rounded-xl shadow-lg max-w-xs z-50">
          <p className="text-xs mb-2">Dev Navigation</p>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <button onClick={() => setCurrentScreen('welcome')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Welcome</button>
            <button onClick={() => setCurrentScreen('signup')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">SignUp</button>
            <button onClick={() => setCurrentScreen('kyc1')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">KYC 1</button>
            <button onClick={() => setCurrentScreen('kyc2')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">KYC 2</button>
            <button onClick={() => setCurrentScreen('biometric')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Biometric</button>
            <button onClick={() => setCurrentScreen('home')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Home</button>
            <button onClick={() => setCurrentScreen('conversion')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Convert</button>
            <button onClick={() => setCurrentScreen('p2p')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">P2P</button>
            <button onClick={() => setCurrentScreen('campus')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Campus</button>
            <button onClick={() => setCurrentScreen('remittance')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Remittance</button>
            <button onClick={() => setCurrentScreen('admin')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Admin</button>
            <button onClick={() => setCurrentScreen('alerts')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Alerts</button>
            <button onClick={() => setCurrentScreen('analytics')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded">Analytics</button>
          </div>
        </div>
      )}
    </div>
  );
}

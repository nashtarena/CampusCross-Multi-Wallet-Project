import React, { useState, createContext, useContext } from 'react';
import { Welcome } from './components/screens/Welcome';
import { SignUp } from './components/screens/SignUp';
import { KYCTier1 } from './components/screens/KYCTier1';
import { KYCTier2 } from './components/screens/KYCTier2';
import { KYCStatusPolling } from './components/screens/KYCStatusPolling';
import { Home } from './components/screens/Home';
import { P2PTransfer } from './components/screens/P2PTransfer';
import { CurrencyConversion } from './components/screens/CurrencyConversion';
import { CampusPayments } from './components/screens/CampusPayments';
import { Remittance } from './components/screens/Remittance';
import { AdminPanel } from './components/screens/AdminPanel';
import { AdminLogin } from './components/screens/AdminLogin';
import { RateAlerts } from './components/screens/RateAlerts';
import { CreateWallet } from './components/screens/CreateWallet';
import { Toaster } from './components/ui/sonner';

type Screen = 
  | 'welcome' 
  | 'signup' 
  | 'kyc1' 
  | 'kyc2' 
  | 'kyc-polling'
  | 'home' 
  | 'conversion' 
  | 'p2p' 
  | 'campus' 
  | 'remittance' 
  | 'admin-login'
  | 'admin' 
  | 'alerts' 
  | 'settings'
  | 'createWallet';

interface AppContextType {
  userName: string;
  setUserName: (name: string) => void;
  userPassword: string;
  setUserPassword: (password: string) => void;
  userId: number;
  setUserId: (id: number) => void;
  theme: 'light' | 'dark';
  toggleTheme: () => void;
  logout: () => void;
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
  const [userName, setUserName] = useState(() => localStorage.getItem('userName') || '');
  const [userPassword, setUserPassword] = useState(() => localStorage.getItem('userPassword') || '');
  const [userId, setUserId] = useState(9002);
  const [accessToken, setAccessToken] = useState('');
  const [theme, setTheme] = useState<'light' | 'dark'>(() => (localStorage.getItem('theme') as 'light' | 'dark') || 'light');

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };

  const logout = () => {
    setUserName('');
    setUserPassword('');
    localStorage.removeItem('userName');
    localStorage.removeItem('userPassword');
    localStorage.removeItem('authToken');
    setCurrentScreen('welcome');
  };
  React.useEffect(() => {
    if (userName) {
      localStorage.setItem('userName', userName);
    }
  }, [userName]);

  React.useEffect(() => {
    if (userPassword) {
      localStorage.setItem('userPassword', userPassword);
    }
  }, [userPassword]);

  React.useEffect(() => {
    const hash = window.location.hash.slice(1);
    if (hash === 'home') {
      setCurrentScreen('home');
    }
  }, []);

  // Listen for programmatic navigation events from child components
  React.useEffect(() => {
    const handler = (e: Event) => {
      try {
        const custom = e as CustomEvent;
        const screen = custom.detail?.screen as Screen | undefined;
        if (screen) setCurrentScreen(screen);
      } catch (err) {
        // ignore malformed events
      }
    };
    window.addEventListener('navigate', handler as EventListener);
    return () => window.removeEventListener('navigate', handler as EventListener);
  }, []);

  const renderScreen = () => {
    switch (currentScreen) {
      case 'welcome':
        return <Welcome onNext={() => setCurrentScreen('signup')} onNavigateToHome={() => {
          const userStr = localStorage.getItem('user');
          console.log('Navigation triggered - localStorage user:', userStr);
          if (userStr) {
            const user = JSON.parse(userStr);
            console.log('Parsed user for navigation:', user);
            console.log('User role for routing:', user.role);
            if (user.role === 'ADMIN') {
              console.log('✅ Routing to admin dashboard');
              setCurrentScreen('admin');
            } else {
              console.log('❌ Routing to home page (role is not ADMIN)');
              setCurrentScreen('home');
            }
          } else {
            console.log('❌ No user data found, routing to home page');
            setCurrentScreen('home');
          }
        }} />;
      case 'signup':
        return (
          <SignUp 
            onBack={() => setCurrentScreen('welcome')}
            onNext={() => setCurrentScreen('kyc1')}
            onAdminSuccess={() => setCurrentScreen('admin')}
          />
        );
      case 'kyc1':
        return (
          <KYCTier1 
            userId={userId}
            onBack={() => setCurrentScreen('signup')}
            onNext={(token, applicantId) => {
              setAccessToken(token);
              console.log('Received access token:', token.substring(0, 30) + '...');
              console.log('Applicant ID:', applicantId);
              setCurrentScreen('kyc2');
            }}
          />
        );
      case 'kyc2':
        return (
          <KYCTier2 
            accessToken={accessToken}
            onComplete={() => {
              console.log('Documents uploaded to Sumsub');
              setCurrentScreen('kyc-polling');
            }}
            onError={(error) => {
              console.error('KYC Tier 2 error:', error);
              alert(error);
            }}
          />
        );
      case 'kyc-polling':
        return (
          <KYCStatusPolling 
            userId={userId}
            onVerified={() => {
              console.log('✅ KYC Verified! User is Tier 2+');
              setCurrentScreen('home');
            }}
            onRejected={(reason) => {
              console.error('❌ KYC Rejected:', reason);
              alert(`Verification failed: ${reason}`);
              setCurrentScreen('kyc1');
            }}
          />
        );
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
      case 'admin-login':
        return <AdminLogin onBack={() => setCurrentScreen('home')} onSuccess={() => setCurrentScreen('admin')} />;
      case 'admin':
        return <AdminPanel onBack={() => setCurrentScreen('home')} />;
      case 'alerts':
        return <RateAlerts onBack={() => setCurrentScreen('home')} userId={userId} />;
      case 'createWallet':
        return <CreateWallet onBack={() => setCurrentScreen('home')} onWalletCreated={() => setCurrentScreen('home')} />;
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
      toggleTheme,
      logout 
    }}>
      <div className="max-w-md mx-auto bg-white min-h-screen shadow-2xl">
        {renderScreen()}
        <Toaster position="top-center" />
        
        {process.env.NODE_ENV === 'development' && (
          <div className="fixed bottom-4 right-4 bg-black/80 backdrop-blur-sm text-white p-4 rounded-xl shadow-lg max-w-xs z-50">
            <p className="text-xs mb-2 font-semibold">Dev Navigation</p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button onClick={() => setCurrentScreen('welcome')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Welcome</button>
              <button onClick={() => setCurrentScreen('signup')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">SignUp</button>
              <button onClick={() => setCurrentScreen('kyc1')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">KYC 1</button>
              <button onClick={() => setCurrentScreen('kyc2')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">KYC 2</button>
              <button onClick={() => setCurrentScreen('kyc-polling')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Polling</button>
              <button onClick={() => setCurrentScreen('home')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Home</button>
              <button onClick={() => setCurrentScreen('conversion')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Convert</button>
              <button onClick={() => setCurrentScreen('p2p')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">P2P</button>
              <button onClick={() => setCurrentScreen('campus')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Campus</button>
              <button onClick={() => setCurrentScreen('remittance')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Remittance</button>
              <button onClick={() => setCurrentScreen('admin-login')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Admin</button>
              <button onClick={() => setCurrentScreen('alerts')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Alerts</button>
              <button onClick={() => setCurrentScreen('createWallet')} className="bg-white/10 hover:bg-white/20 px-2 py-1 rounded transition">Create Wallet</button>
            </div>
          </div>
        )}
      </div>
    </AppContext.Provider>
  );
}

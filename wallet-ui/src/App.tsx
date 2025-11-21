import React, { useState, createContext, useContext } from "react";
import { Welcome } from "./components/screens/Welcome";
import { SignUp } from "./components/screens/SignUp";
import { KYCTier1 } from "./components/screens/KYCTier1";
import { KYCTier2 } from "./components/screens/KYCTier2";
import { KYCStatusPolling } from "./components/screens/KYCStatusPolling";
import { Home } from "./components/screens/Home";
import MerchantDashboard from "./components/screens/MerchantDashboard";
import { P2PTransfer } from "./components/screens/P2PTransfer";
import { CurrencyConversion } from "./components/screens/CurrencyConversion";
import { Remittance } from "./components/screens/Remittance";
import { AdminPanel } from "./components/screens/AdminPanel";
import { RateAlerts } from "./components/screens/RateAlerts";
import { CreateWallet } from "./components/screens/CreateWallet";
import { Toaster } from "./components/ui/sonner";

type Screen =
  | "welcome"
  | "signup"
  | "kyc1"
  | "kyc2"
  | "kyc-polling"
  | "home"
  | "conversion"
  | "p2p"
  | "campus"
  | "remittance"
  | "admin-login"
  | "admin"
  | "alerts"
  | "settings"
  | "merchant"
  | "createWallet";

interface AppContextType {
  userName: string;
  setUserName: (name: string) => void;
  userPassword: string;
  setUserPassword: (password: string) => void;
  userId: number;
  setUserId: (id: number) => void;
  theme: "light" | "dark";
  toggleTheme: () => void;
  logout: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useAppContext = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error("useAppContext must be used within AppProvider");
  }
  return context;
};

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>("welcome");
  const [userName, setUserName] = useState(
    () => localStorage.getItem("userName") || ""
  );
  const [userPassword, setUserPassword] = useState(
    () => localStorage.getItem("userPassword") || ""
  );
  const [userId, setUserId] = useState(localStorage.getItem("userId") ? parseInt(localStorage.getItem("userId") as string) : 0);
  const [accessToken, setAccessToken] = useState("");
  const [theme, setTheme] = useState<"light" | "dark">(
    () => (localStorage.getItem("theme") as "light" | "dark") || "light"
  );

  const toggleTheme = () => {
    const newTheme = theme === "light" ? "dark" : "light";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme);
  };

  const logout = () => {
    setUserName("");
    setUserPassword("");
    localStorage.removeItem("userName");
    localStorage.removeItem("userPassword");
    localStorage.removeItem("authToken");
    setCurrentScreen("welcome");
  };
  React.useEffect(() => {
    if (userName) {
      localStorage.setItem("userName", userName);
    }
  }, [userName]);

  React.useEffect(() => {
    if (userPassword) {
      localStorage.setItem("userPassword", userPassword);
    }
  }, [userPassword]);

  React.useEffect(() => {
    const hash = window.location.hash.slice(1);
    if (hash === "home") {
      setCurrentScreen("home");
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
    window.addEventListener("navigate", handler as EventListener);
    return () =>
      window.removeEventListener("navigate", handler as EventListener);
  }, []);

  const renderScreen = () => {
    switch (currentScreen) {
      case "welcome":
        return (
          <Welcome
            onNext={() => setCurrentScreen("signup")}
            onNavigateToHome={() => {
              const userStr = localStorage.getItem("user");
              console.log("Navigation triggered - localStorage user:", userStr);
              if (userStr) {
                const user = JSON.parse(userStr);
                console.log("Parsed user for navigation:", user);
                console.log("User role for routing:", user.role);
                if (user.role === "ADMIN") {
                  console.log("✅ Routing to admin dashboard");
                  setCurrentScreen("admin");
                } else if (user.role === "MERCHANT") {
                  console.log("✅ Routing to merchant dashboard");
                  setCurrentScreen("merchant");
                } else {
                  console.log("❌ Routing to home page (role is not ADMIN)");
                  setCurrentScreen("home");
                }
              } else {
                console.log("❌ No user data found, routing to home page");
                setCurrentScreen("home");
              }
            }}
          />
        );
      case "signup":
        return (
          <SignUp
            onBack={() => setCurrentScreen("welcome")}
            onNext={() => setCurrentScreen("kyc1")}
            onAdminSuccess={() => setCurrentScreen("admin")}
          />
        );
      case "kyc1":
        return (
          <KYCTier1
            userId={userId}
            onBack={() => setCurrentScreen("signup")}
            onNext={(token, applicantId) => {
              setAccessToken(token);
              console.log(
                "Received access token:",
                token.substring(0, 30) + "..."
              );
              console.log("Applicant ID:", applicantId);
              setCurrentScreen("kyc2");
            }}
          />
        );
      case "kyc2":
        return (
          <KYCTier2
            accessToken={accessToken}
            onComplete={() => {
              console.log("Documents uploaded to Sumsub");
              setCurrentScreen("kyc-polling");
            }}
            onError={(error) => {
              console.error("KYC Tier 2 error:", error);
              alert(error);
            }}
          />
        );
      // In your App.tsx file, find the 'kyc-polling' case and replace it with this:

      case "kyc-polling":
        return (
          <KYCStatusPolling
            userId={userId}
            onVerified={() => {
              console.log("✅ KYC Verified! Routing based on user role...");

              // Get user role from localStorage
              const userStr = localStorage.getItem("user");
              if (userStr) {
                try {
                  const user = JSON.parse(userStr);
                  const userRole = user.role;

                  console.log("User role:", userRole);

                  // Update KYC status in localStorage
                  user.kycStatus = "VERIFIED";
                  localStorage.setItem("user", JSON.stringify(user));

                  // Navigate based on role
                  if (userRole === "MERCHANT") {
                    console.log("✅ Routing merchant to merchant dashboard");
                    setCurrentScreen("merchant");
                  } else if (userRole === "ADMIN") {
                    console.log("✅ Routing admin to admin dashboard");
                    setCurrentScreen("admin");
                  } else {
                    console.log("✅ Routing student to home dashboard");
                    setCurrentScreen("home");
                  }
                } catch (error) {
                  console.error("Failed to parse user data:", error);
                  setCurrentScreen("home"); // Fallback to home
                }
              } else {
                console.log("❌ No user data found, routing to home");
                setCurrentScreen("home");
              }
            }}
            onRejected={(reason) => {
              console.error("❌ KYC Rejected:", reason);
              alert(`Verification failed: ${reason}`);
              setCurrentScreen("kyc1");
            }}
          />
        );
      case "home":
        return (
          <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />
        );
      case "merchant":
        return (
          <MerchantDashboard
            onNavigate={(screen) => setCurrentScreen(screen as Screen)}
          />
        );
      case "conversion":
        return <CurrencyConversion onBack={() => setCurrentScreen("home")} />;
      case "p2p":
        return <P2PTransfer onBack={() => setCurrentScreen("home")} />;
      case "remittance":
        return <Remittance onBack={() => setCurrentScreen("home")} />;
      case "admin":
        return <AdminPanel />;
      case "alerts":
        return (
          <RateAlerts onBack={() => setCurrentScreen("home")} userId={userId} />
        );
      case "createWallet":
        return (
          <CreateWallet
            onBack={() => setCurrentScreen("home")}
            onWalletCreated={() => setCurrentScreen("home")}
          />
        );
      case "settings":
        return (
          <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />
        );
      default:
        return <Welcome onNext={() => setCurrentScreen("signup")} />;
    }
  };

  return (
    <AppContext.Provider
      value={{
        userName,
        setUserName,
        userPassword,
        setUserPassword,
        userId,
        setUserId,
        theme,
        toggleTheme,
        logout,
      }}
    >
      <div className="max-w-md mx-auto bg-white min-h-screen shadow-2xl">
        {renderScreen()}
        <Toaster position="top-center" />
      </div>
    </AppContext.Provider>
  );
}

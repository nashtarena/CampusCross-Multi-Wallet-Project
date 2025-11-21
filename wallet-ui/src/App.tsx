import React, { useEffect, useState, createContext, useContext } from "react";
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
  if (!context) throw new Error("useAppContext must be used within AppProvider");
  return context;
};

/**
 * Helper: parse localStorage "user" object to extract id and username.
 * Returns { id: number | 0, userName: string | "" }.
 */
function parseStoredUser(): { id: number; userName: string } {
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return { id: 0, userName: "" };
    const u = JSON.parse(raw);
    const id = Number(u?.id ?? u?.userId ?? u?.studentId ?? 0) || 0;
    const userName = String(u?.name ?? u?.username ?? u?.email ?? "") || "";
    return { id, userName };
  } catch {
    return { id: 0, userName: "" };
  }
}

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>("welcome");

  const [userName, setUserName] = useState<string>(() => {
    // Prefer value from stored "user", fallback to userName key
    const parsed = parseStoredUser();
    if (parsed.userName) return parsed.userName;
    return localStorage.getItem("userName") || "";
  });

  const [userPassword, setUserPassword] = useState<string>(() => {
    return localStorage.getItem("userPassword") || "";
  });

  // userId is 0 when not logged in. We will keep it in sync with localStorage "user".
  const [userId, setUserId] = useState<number>(() => {
    const parsed = parseStoredUser();
    if (parsed.id) return parsed.id;
    // if there's an explicit userId key (legacy), prefer it
    const legacy = localStorage.getItem("userId");
    return legacy ? parseInt(legacy, 10) || 0 : 0;
  });

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
    setUserId(0);
    localStorage.removeItem("user");
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userPassword");
    localStorage.removeItem("authToken");
    setCurrentScreen("welcome");
    // Let other parts of the app know the user changed
    window.dispatchEvent(new CustomEvent("localstorage-set", { detail: { key: "user", value: null } }));
  };

  // Persist userName and userPassword if they change (optional)
  useEffect(() => {
    if (userName) localStorage.setItem("userName", userName);
  }, [userName]);

  useEffect(() => {
    if (userPassword) localStorage.setItem("userPassword", userPassword);
  }, [userPassword]);

  // Persist userId separately for legacy usage or external libs
  useEffect(() => {
    if (userId && userId > 0) {
      localStorage.setItem("userId", String(userId));
    } else {
      localStorage.removeItem("userId");
    }
  }, [userId]);

  // On mount: ensure same-tab writes to localStorage will dispatch an event we can listen to.
  useEffect(() => {
    const originalSetItem = Storage.prototype.setItem;

    // Patch once on mount.
    Storage.prototype.setItem = function (key: string, value: string) {
      // call original
      originalSetItem.apply(this, [key, value]);
      // Dispatch a small CustomEvent indicating same-tab change
      try {
        window.dispatchEvent(new CustomEvent("localstorage-set", { detail: { key, value } }));
      } catch {
        // ignore if environment blocks CustomEvent
      }
    };

    return () => {
      // restore
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      Storage.prototype.setItem = originalSetItem;
    };
  }, []);

  // Sync: whenever "user" changes in localStorage (cross-tab via storage event OR same-tab via our patched setItem),
  // parse it and update userId and userName.
  useEffect(() => {
    const syncFromStorage = () => {
      const parsed = parseStoredUser();
      if (parsed.id !== userId) setUserId(parsed.id);
      if (parsed.userName !== userName) setUserName(parsed.userName);
    };

    const storageHandler = (e: StorageEvent) => {
      // if key is "user" or null (clear), sync
      if (!e.key || e.key === "user" || e.key === "userId") {
        syncFromStorage();
      }
    };

    const sameTabHandler = (ev: Event) => {
      try {
        const ce = ev as CustomEvent;
        const key = ce.detail?.key as string | undefined;
        if (!key || key === "user" || key === "userId") {
          syncFromStorage();
        }
      } catch {
        // ignore
      }
    };

    window.addEventListener("storage", storageHandler);
    window.addEventListener("localstorage-set", sameTabHandler);

    // Also run once on mount to ensure we pick up the latest value.
    syncFromStorage();

    return () => {
      window.removeEventListener("storage", storageHandler);
      window.removeEventListener("localstorage-set", sameTabHandler);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // empty deps: we use setState inside

  // Hash-based navigation (unchanged)
  useEffect(() => {
    const hash = window.location.hash.slice(1);
    if (hash === "home") setCurrentScreen("home");
  }, []);

  // Listen for programmatic navigation events from children (unchanged)
  useEffect(() => {
    const handler = (e: Event) => {
      try {
        const custom = e as CustomEvent;
        const screen = custom.detail?.screen as Screen | undefined;
        if (screen) setCurrentScreen(screen);
      } catch {
        // ignore malformed events
      }
    };
    window.addEventListener("navigate", handler as EventListener);
    return () => window.removeEventListener("navigate", handler as EventListener);
  }, []);

  const renderScreen = () => {
    switch (currentScreen) {
      case "welcome":
        return (
          <Welcome
            onNext={() => setCurrentScreen("signup")}
            onNavigateToHome={() => {
              const userStr = localStorage.getItem("user");
              if (userStr) {
                try {
                  const user = JSON.parse(userStr);
                  if (user.role === "ADMIN") setCurrentScreen("admin");
                  else if (user.role === "MERCHANT") setCurrentScreen("merchant");
                  else setCurrentScreen("home");
                } catch {
                  setCurrentScreen("home");
                }
              } else {
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
            onNext={(token) => {
              setAccessToken(token);
              setCurrentScreen("kyc2");
            }}
          />
        );

      case "kyc2":
        return (
          <KYCTier2
            accessToken={accessToken}
            onComplete={() => setCurrentScreen("kyc-polling")}
            onError={(error) => {
              console.error(error);
              alert(String(error));
            }}
          />
        );

      case "kyc-polling":
        return (
          <KYCStatusPolling
            userId={userId}
            onVerified={() => {
              const userStr = localStorage.getItem("user");
              if (!userStr) return setCurrentScreen("home");
              try {
                const user = JSON.parse(userStr);
                user.kycStatus = "VERIFIED";
                localStorage.setItem("user", JSON.stringify(user)); // will trigger localstorage-set
                if (user.role === "MERCHANT") setCurrentScreen("merchant");
                else if (user.role === "ADMIN") setCurrentScreen("admin");
                else setCurrentScreen("home");
              } catch {
                setCurrentScreen("home");
              }
            }}
            onRejected={() => {
              alert("KYC rejected");
              setCurrentScreen("kyc1");
            }}
          />
        );

      case "home":
        return <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;

      case "merchant":
        return <MerchantDashboard onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;

      case "conversion":
        return <CurrencyConversion onBack={() => setCurrentScreen("home")} />;

      case "p2p":
        return <P2PTransfer onBack={() => setCurrentScreen("home")} />;
      case "remittance":
        return <Remittance onBack={() => setCurrentScreen("home")} />;

      case "admin":
        return <AdminPanel />;
      case "alerts":
        // Pass the latest userId prop (keeps RateAlerts behavior unchanged).
        return <RateAlerts onBack={() => setCurrentScreen("home")} userId={userId} />;

      case "createWallet":
        return <CreateWallet onBack={() => setCurrentScreen("home")} onWalletCreated={() => setCurrentScreen("home")} />;

      case "settings":
        return <Home onNavigate={(screen) => setCurrentScreen(screen as Screen)} />;

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

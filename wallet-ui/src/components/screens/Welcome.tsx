import React from "react";
import { Button } from "../ui/button";
import {
  CheckCircle,
  ArrowUpRight,
  Shield,
  Home,
  Sparkles,
} from "lucide-react";

interface WelcomeProps {
  onNext: () => void;
  onNavigateToSignIn: () => void; // Now required, not optional
}

export function Welcome({ onNext, onNavigateToSignIn }: WelcomeProps) {

  const handleSignIn = async () => {
    if (!signInData.studentId || !signInData.password) {
      toast.error(
        `Please enter both ${
          signInData.role === "ADMIN"
            ? "admin ID"
            : signInData.role === "MERCHANT"
            ? "merchant ID"
            : "student ID"
        } and password`
      );
      return;
    }

    setIsLoading(true);

    try {
      const response = await authApi.login(
        signInData.studentId,
        signInData.password,
        signInData.role
      );

      localStorage.setItem("authToken", response.token);

      const userData = {
        id: response.userId,
        email: response.email,
        fullName: response.fullName,
        role: response.role,
        status: response.status,
        kycStatus: response.kycStatus || "NOT_STARTED",
        campusName: response.campusName,
      };

      localStorage.setItem("user", JSON.stringify(userData));
      setUserName(response.fullName);
      setUserPassword(signInData.password);
      toast.success("Login successful!");
      setShowSignIn(false);

      let targetScreen = "home";
      if (response.role === "ADMIN") {
        targetScreen = "admin";
      } else if (response.role === "MERCHANT") {
        targetScreen = "merchant";
      }

      window.dispatchEvent(
        new CustomEvent("navigate", {
          detail: { screen: targetScreen },
        })
      );
    } catch (error) {
      console.error("Login error:", error);
      toast.error(error instanceof Error ? error.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ 
      minHeight: "100vh", 
      background: "linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #312e81 100%)",
      position: "relative",
      overflow: "hidden"
    }}>
      {/* Animated Background Elements */}
      <div style={{
        position: "absolute",
        top: "-10%",
        left: "-5%",
        width: "500px",
        height: "500px",
        background: "radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%)",
        borderRadius: "50%",
        animation: "float 20s infinite ease-in-out"
      }}></div>
      <div style={{
        position: "absolute",
        bottom: "-10%",
        right: "-5%",
        width: "600px",
        height: "600px",
        background: "radial-gradient(circle, rgba(168,85,247,0.15) 0%, transparent 70%)",
        borderRadius: "50%",
        animation: "float 25s infinite ease-in-out reverse"
      }}></div>
      <div style={{
        position: "absolute",
        top: "50%",
        left: "50%",
        width: "400px",
        height: "400px",
        background: "radial-gradient(circle, rgba(59,130,246,0.1) 0%, transparent 70%)",
        borderRadius: "50%",
        animation: "pulse 15s infinite ease-in-out"
      }}></div>

      {/* CSS Animations */}
      <style>{`
        @keyframes float {
          0%, 100% { transform: translate(0, 0) rotate(0deg); }
          33% { transform: translate(30px, -30px) rotate(5deg); }
          66% { transform: translate(-20px, 20px) rotate(-5deg); }
        }
        @keyframes pulse {
          0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
          50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.3; }
        }
        @keyframes shimmer {
          0% { background-position: -1000px 0; }
          100% { background-position: 1000px 0; }
        }
      `}</style>

      <div style={{ 
        position: "relative", 
        zIndex: 1,
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        padding: "20px"
      }}>
        <div style={{ width: "100%", maxWidth: "1400px", margin: "0 auto" }}>
          <div style={{ 
            display: "grid", 
            gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
            gap: "60px",
            alignItems: "center"
          }}>
            {/* Left Side - Hero Content */}
            <div style={{ padding: "20px" }}>
              <div style={{
                display: "inline-flex",
                alignItems: "center",
                gap: "8px",
                background: "rgba(255, 255, 255, 0.1)",
                padding: "8px 16px",
                borderRadius: "20px",
                marginBottom: "24px",
                border: "1px solid rgba(255, 255, 255, 0.2)"
              }}>
                <Sparkles size={16} color="#fbbf24" />
                <span style={{ color: "#fbbf24", fontSize: "14px", fontWeight: "600" }}>
                  Trusted by 10,000+ Students
                </span>
              </div>

              <h1 style={{ 
                fontSize: "clamp(32px, 5vw, 52px)", 
                fontWeight: "800", 
                color: "white", 
                marginBottom: "20px",
                lineHeight: "1.1",
                background: "linear-gradient(to right, #ffffff, #e0e7ff)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text"
              }}>
                Banking Designed for Students Who Cross Borders
              </h1>
              <p style={{ fontSize: "20px", color: "#c7d2fe", marginBottom: "8px", fontWeight: "500" }}>
                Manage money across currencies.
              </p>
              <p style={{ fontSize: "20px", color: "#c7d2fe", marginBottom: "36px", fontWeight: "500" }}>
                Pay globally. Spend locally.
              </p>
              <div style={{ 
                display: "flex", 
                alignItems: "center", 
                gap: "16px",
                maxWidth: "320px",
                width: "100%"
              }}>
                <Button
                  onClick={onNext}
                  style={{
                    background: "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)",
                    color: "#1e3a8a",
                    height: "56px",
                    padding: "0 40px",
                    borderRadius: "28px",
                    fontSize: "18px",
                    fontWeight: "700",
                    border: "none",
                    cursor: "pointer",
                    boxShadow: "0 10px 40px rgba(251, 191, 36, 0.4)",
                    flex: "1",
                    transition: "all 0.3s ease"
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = "translateY(-2px)";
                    e.currentTarget.style.boxShadow = "0 15px 50px rgba(251, 191, 36, 0.5)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = "translateY(0)";
                    e.currentTarget.style.boxShadow = "0 10px 40px rgba(251, 191, 36, 0.4)";
                  }}
                >
                  Create your wallet
                </Button>
                <button
                  onClick={onNavigateToSignIn}
                  style={{
                    background: "transparent",
                    color: "#e0e7ff",
                    fontSize: "16px",
                    border: "none",
                    cursor: "pointer",
                    textDecoration: "underline",
                    padding: "8px 16px",
                    fontWeight: "500",
                    whiteSpace: "nowrap"
                  }}
                >
                  Sign In
                </button>
              </div>
            </div>

            {/* Right Side - Wallet Visual */}
            <div style={{ padding: "20px", display: "flex", justifyContent: "center" }}>
              <div style={{ position: "relative", width: "340px", height: "280px" }}>
                {/* Enhanced Wallet Card */}
                <div style={{
                  width: "340px",
                  height: "220px",
                  background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%)",
                  borderRadius: "24px",
                  boxShadow: "0 25px 70px rgba(139, 92, 246, 0.4), 0 0 0 1px rgba(255,255,255,0.1)",
                  position: "relative",
                  overflow: "hidden",
                  border: "1px solid rgba(255, 255, 255, 0.1)"
                }}>
                  {/* Shine effect */}
                  <div style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: "linear-gradient(120deg, transparent 0%, rgba(255,255,255,0.15) 50%, transparent 100%)",
                    backgroundSize: "200% 100%",
                    animation: "shimmer 3s infinite"
                  }}></div>

                  {/* Decorative grid pattern */}
                  <div style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundImage: "radial-gradient(circle at 20px 20px, rgba(255,255,255,0.05) 1px, transparent 1px)",
                    backgroundSize: "40px 40px"
                  }}></div>

                  {/* Decorative circles */}
                  <div style={{
                    position: "absolute",
                    right: "-40px",
                    top: "-40px",
                    width: "140px",
                    height: "140px",
                    background: "radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%)",
                    borderRadius: "50%"
                  }}></div>
                  <div style={{
                    position: "absolute",
                    left: "-20px",
                    bottom: "-20px",
                    width: "100px",
                    height: "100px",
                    background: "radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%)",
                    borderRadius: "50%"
                  }}></div>

                  {/* Card Details */}
                  <div style={{
                    position: "absolute",
                    top: "24px",
                    left: "24px",
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}>
                    <div style={{
                      width: "40px",
                      height: "32px",
                      background: "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)",
                      borderRadius: "6px",
                      border: "1px solid rgba(255,255,255,0.3)"
                    }}></div>
                    <div style={{
                      fontSize: "12px",
                      color: "rgba(255,255,255,0.9)",
                      fontWeight: "600",
                      letterSpacing: "0.5px"
                    }}>
                      CAMPUS CROSS
                    </div>
                  </div>

                  <div style={{
                    position: "absolute",
                    bottom: "24px",
                    left: "24px",
                    right: "24px"
                  }}>
                    <div style={{
                      fontSize: "22px",
                      color: "white",
                      fontWeight: "700",
                      letterSpacing: "2px",
                      marginBottom: "12px",
                      fontFamily: "monospace"
                    }}>
                      •••• •••• •••• 4242
                    </div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <div>
                        <div style={{ fontSize: "10px", color: "rgba(255,255,255,0.7)", marginBottom: "4px" }}>
                          BALANCE
                        </div>
                        <div style={{ fontSize: "16px", color: "white", fontWeight: "700" }}>
                          $12,450.00
                        </div>
                      </div>
                      <div style={{
                        width: "48px",
                        height: "48px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center"
                      }}>
                        <div style={{ display: "flex", gap: "4px" }}>
                          <div style={{
                            width: "20px",
                            height: "20px",
                            borderRadius: "50%",
                            background: "rgba(251, 191, 36, 0.8)",
                            border: "2px solid white"
                          }}></div>
                          <div style={{
                            width: "20px",
                            height: "20px",
                            borderRadius: "50%",
                            background: "rgba(239, 68, 68, 0.8)",
                            border: "2px solid white",
                            marginLeft: "-10px"
                          }}></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Enhanced Floating Coins */}
                <div style={{
                  position: "absolute",
                  top: "-32px",
                  right: "-32px",
                  width: "80px",
                  height: "80px",
                  background: "linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%)",
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "32px",
                  fontWeight: "800",
                  color: "#78350f",
                  boxShadow: "0 12px 30px rgba(251, 191, 36, 0.4)",
                  border: "3px solid rgba(255, 255, 255, 0.3)"
                }}>
                  $
                </div>
                <div style={{
                  position: "absolute",
                  top: "18px",
                  left: "-44px",
                  width: "68px",
                  height: "68px",
                  background: "linear-gradient(135deg, #fcd34d 0%, #fbbf24 100%)",
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "26px",
                  fontWeight: "800",
                  color: "#78350f",
                  boxShadow: "0 10px 25px rgba(252, 211, 77, 0.4)",
                  border: "3px solid rgba(255, 255, 255, 0.3)"
                }}>
                  €
                </div>
                <div style={{
                  position: "absolute",
                  bottom: "-16px",
                  right: "50px",
                  width: "58px",
                  height: "58px",
                  background: "linear-gradient(135deg, #f59e0b 0%, #d97706 100%)",
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "22px",
                  fontWeight: "800",
                  color: "#78350f",
                  boxShadow: "0 8px 20px rgba(245, 158, 11, 0.4)",
                  border: "3px solid rgba(255, 255, 255, 0.3)"
                }}>
                  £
                </div>
              </div>
            </div>
          </div>

          {/* Features Bar - Single Line */}
          <div style={{
            marginTop: "60px",
            padding: "32px",
            background: "rgba(255, 255, 255, 0.05)",
            backdropFilter: "blur(20px)",
            borderRadius: "20px",
            border: "1px solid rgba(255, 255, 255, 0.1)",
            boxShadow: "0 8px 32px rgba(0, 0, 0, 0.2)"
          }}>
            <div style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
              gap: "32px"
            }}>
              {/* Feature 1 */}
              <div style={{ textAlign: "center" }}>
                <div style={{
                  width: "52px",
                  height: "52px",
                  borderRadius: "50%",
                  background: "linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  margin: "0 auto 12px",
                  boxShadow: "0 8px 20px rgba(59, 130, 246, 0.3)"
                }}>
                  <CheckCircle color="white" size={26} />
                </div>
                <h3 style={{ fontSize: "15px", fontWeight: "700", color: "white", marginBottom: "6px" }}>
                  Multi-Currency Wallets
                </h3>
                <p style={{ fontSize: "13px", color: "#c7d2fe", lineHeight: "1.4" }}>
                  Manage 5 currencies with real-time rates
                </p>
              </div>

              {/* Feature 2 */}
              <div style={{ textAlign: "center" }}>
                <div style={{
                  width: "52px",
                  height: "52px",
                  borderRadius: "50%",
                  background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  margin: "0 auto 12px",
                  boxShadow: "0 8px 20px rgba(99, 102, 241, 0.3)"
                }}>
                  <ArrowUpRight color="white" size={26} />
                </div>
                <h3 style={{ fontSize: "15px", fontWeight: "700", color: "white", marginBottom: "6px" }}>
                  Lightning Fast P2P
                </h3>
                <p style={{ fontSize: "13px", color: "#c7d2fe", lineHeight: "1.4" }}>
                  Instant transfers with zero fees
                </p>
              </div>

              {/* Feature 3 */}
              <div style={{ textAlign: "center" }}>
                <div style={{
                  width: "52px",
                  height: "52px",
                  borderRadius: "50%",
                  background: "linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  margin: "0 auto 12px",
                  boxShadow: "0 8px 20px rgba(139, 92, 246, 0.3)"
                }}>
                  <Shield color="white" size={26} />
                </div>
                <h3 style={{ fontSize: "15px", fontWeight: "700", color: "white", marginBottom: "6px" }}>
                  Enterprise Security
                </h3>
                <p style={{ fontSize: "13px", color: "#c7d2fe", lineHeight: "1.4" }}>
                  Bank-grade encryption keeps funds safe
                </p>
              </div>

              {/* Feature 4 */}
              <div style={{ textAlign: "center" }}>
                <div style={{
                  width: "52px",
                  height: "52px",
                  borderRadius: "50%",
                  background: "linear-gradient(135deg, #f97316 0%, #ea580c 100%)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  margin: "0 auto 12px",
                  boxShadow: "0 8px 20px rgba(249, 115, 22, 0.3)"
                }}>
                  <Home color="white" size={26} />
                </div>
                <h3 style={{ fontSize: "15px", fontWeight: "700", color: "white", marginBottom: "6px" }}>
                  Campus Payments
                </h3>
                <p style={{ fontSize: "13px", color: "#c7d2fe", lineHeight: "1.4" }}>
                  Pay merchants and services easily
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
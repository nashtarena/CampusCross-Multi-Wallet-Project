import React, { useState } from "react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "../ui/select";
import { ArrowLeft, Sparkles, User } from "lucide-react";
import { useAppContext } from "../../App";
import { authApi } from "../../services/walletApi";
import { toast } from "sonner";

interface SignInProps {
  onBack: () => void;
}

export function SignIn({ onBack }: SignInProps) {
  const [signInData, setSignInData] = useState({
    studentId: "",
    password: "",
    role: "STUDENT",
  });
  const [isLoading, setIsLoading] = useState(false);
  const { setUserName, setUserPassword } = useAppContext();

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
        id: String(response.userId),
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
      overflow: "hidden",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "20px"
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

      {/* CSS Animations */}
      <style>{`
        @keyframes float {
          0%, 100% { transform: translate(0, 0) rotate(0deg); }
          33% { transform: translate(30px, -30px) rotate(5deg); }
          66% { transform: translate(-20px, 20px) rotate(-5deg); }
        }
        @keyframes shimmer {
          0% { background-position: -1000px 0; }
          100% { background-position: 1000px 0; }
        }
        @keyframes pulse {
          0%, 100% { transform: scale(1); opacity: 1; }
          50% { transform: scale(1.05); opacity: 0.8; }
        }
      `}</style>

      {/* Back Button */}
      <button
        onClick={onBack}
        style={{
          position: "absolute",
          top: "24px",
          left: "24px",
          background: "rgba(255, 255, 255, 0.1)",
          border: "1px solid rgba(255, 255, 255, 0.2)",
          borderRadius: "50%",
          width: "48px",
          height: "48px",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          cursor: "pointer",
          backdropFilter: "blur(10px)",
          transition: "all 0.3s ease",
          zIndex: 10
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = "rgba(255, 255, 255, 0.2)";
          e.currentTarget.style.transform = "translateX(-4px)";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = "rgba(255, 255, 255, 0.1)";
          e.currentTarget.style.transform = "translateX(0)";
        }}
      >
        <ArrowLeft color="white" size={24} />
      </button>

      {/* Sign In Card */}
      <div style={{
        position: "relative",
        zIndex: 1,
        width: "100%",
        maxWidth: "480px",
        background: "rgba(255, 255, 255, 0.95)",
        borderRadius: "32px",
        padding: "48px 40px",
        boxShadow: "0 20px 60px rgba(0, 0, 0, 0.3)",
        backdropFilter: "blur(20px)",
        border: "1px solid rgba(255, 255, 255, 0.2)"
      }}>
        {/* Icon/Logo */}
        <div style={{
          display: "flex",
          justifyContent: "center",
          marginBottom: "24px"
        }}>
          <div style={{
            position: "relative",
            width: "100px",
            height: "100px"
          }}>
            {/* Decorative circles */}
            <div style={{
              position: "absolute",
              top: "10px",
              left: "10px",
              width: "60px",
              height: "60px",
              background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
              borderRadius: "50%",
              animation: "pulse 3s infinite ease-in-out"
            }}></div>
            <div style={{
              position: "absolute",
              top: "20px",
              right: "10px",
              width: "50px",
              height: "50px",
              background: "linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%)",
              borderRadius: "50%",
              animation: "pulse 3s infinite ease-in-out 0.5s"
            }}></div>
            <div style={{
              position: "absolute",
              bottom: "15px",
              right: "25px",
              width: "20px",
              height: "20px",
              background: "linear-gradient(135deg, #3b82f6 0%, #6366f1 100%)",
              borderRadius: "50%",
              animation: "pulse 3s infinite ease-in-out 1s"
            }}></div>
            {/* Center Icon */}
            <div style={{
              position: "absolute",
              top: "50%",
              left: "50%",
              transform: "translate(-50%, -50%)",
              width: "48px",
              height: "48px",
              background: "white",
              borderRadius: "50%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              boxShadow: "0 4px 12px rgba(0, 0, 0, 0.1)",
              zIndex: 1
            }}>
              <User color="#6366f1" size={28} />
            </div>
          </div>
        </div>

        {/* Title */}
        <h1 style={{
          fontSize: "32px",
          fontWeight: "800",
          textAlign: "center",
          color: "#1e293b",
          marginBottom: "8px"
        }}>
          Sign In
        </h1>
        <p style={{
          fontSize: "15px",
          textAlign: "center",
          color: "#64748b",
          marginBottom: "32px"
        }}>
          Welcome back! Please enter your details
        </p>

        {/* Trust Badge */}
        <div style={{
          display: "flex",
          justifyContent: "center",
          marginBottom: "32px"
        }}>
        </div>

        {/* Form */}
        <div style={{ marginBottom: "24px" }}>
          <Label htmlFor="role" style={{ color: "#1e293b", marginBottom: "8px", display: "block", fontSize: "14px", fontWeight: "600" }}>
            I am a
          </Label>
          <Select
            value={signInData.role}
            onValueChange={(value: string) =>
              setSignInData({ ...signInData, role: value })
            }
          >
            <SelectTrigger 
              id="role"
              style={{
                height: "52px",
                borderRadius: "16px",
                border: "2px solid #e2e8f0",
                fontSize: "15px",
                background: "#f8fafc"
              }}
            >
              <SelectValue placeholder="Select your role" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="STUDENT">Student</SelectItem>
              <SelectItem value="ADMIN">Admin</SelectItem>
              <SelectItem value="MERCHANT">Merchant</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div style={{ marginBottom: "20px" }}>
          <Label htmlFor="studentId" style={{ color: "#1e293b", marginBottom: "8px", display: "block", fontSize: "14px", fontWeight: "600" }}>
            {signInData.role === "ADMIN"
              ? "Admin ID"
              : signInData.role === "MERCHANT"
              ? "Merchant ID"
              : "Student ID"}
          </Label>
          <Input
            id="studentId"
            type="text"
            placeholder={
              signInData.role === "ADMIN"
                ? "Enter your admin ID"
                : signInData.role === "MERCHANT"
                ? "Enter your merchant ID"
                : "Enter your student ID"
            }
            value={signInData.studentId}
            onChange={(e) =>
              setSignInData({ ...signInData, studentId: e.target.value })
            }
            style={{
              height: "52px",
              borderRadius: "16px",
              border: "2px solid #e2e8f0",
              fontSize: "15px",
              padding: "0 16px",
              background: "#f8fafc"
            }}
            onKeyDown={(e) => e.key === "Enter" && handleSignIn()}
          />
        </div>

        <div style={{ marginBottom: "12px" }}>
          <Label htmlFor="signin-password" style={{ color: "#1e293b", marginBottom: "8px", display: "block", fontSize: "14px", fontWeight: "600" }}>
            Password
          </Label>
          <Input
            id="signin-password"
            type="password"
            placeholder="Enter your password"
            value={signInData.password}
            onChange={(e) =>
              setSignInData({ ...signInData, password: e.target.value })
            }
            style={{
              height: "52px",
              borderRadius: "16px",
              border: "2px solid #e2e8f0",
              fontSize: "15px",
              padding: "0 16px",
              background: "#f8fafc"
            }}
            onKeyDown={(e) => e.key === "Enter" && handleSignIn()}
          />
        </div>

        {/* Forgot Password */}
        <div style={{ textAlign: "right", marginBottom: "24px" }}>
          <button
            style={{
              background: "transparent",
              border: "none",
              color: "#6366f1",
              fontSize: "14px",
              fontWeight: "600",
              cursor: "pointer",
              textDecoration: "none"
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.textDecoration = "underline";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.textDecoration = "none";
            }}
          >
            Forgot password?
          </button>
        </div>

        {/* Sign In Button */}
        <Button
          onClick={handleSignIn}
          disabled={isLoading}
          style={{
            width: "100%",
            height: "56px",
            borderRadius: "16px",
            background: "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)",
            color: "white",
            fontSize: "16px",
            fontWeight: "700",
            border: "none",
            cursor: isLoading ? "not-allowed" : "pointer",
            boxShadow: "0 10px 30px rgba(99, 102, 241, 0.3)",
            transition: "all 0.3s ease",
            opacity: isLoading ? 0.7 : 1
          }}
          onMouseEnter={(e) => {
            if (!isLoading) {
              e.currentTarget.style.transform = "translateY(-2px)";
              e.currentTarget.style.boxShadow = "0 15px 40px rgba(99, 102, 241, 0.4)";
            }
          }}
          onMouseLeave={(e) => {
            if (!isLoading) {
              e.currentTarget.style.transform = "translateY(0)";
              e.currentTarget.style.boxShadow = "0 10px 30px rgba(99, 102, 241, 0.3)";
            }
          }}
        >
          {isLoading ? "Signing In..." : "Sign In"}
        </Button>

        {/* Create Account Link */}
        <div style={{ 
          textAlign: "center", 
          marginTop: "24px",
          fontSize: "14px",
          color: "#64748b"
        }}>
          Don't have an account?{" "}
          <button
            onClick={onBack}
            style={{
              background: "transparent",
              border: "none",
              color: "#6366f1",
              fontWeight: "600",
              cursor: "pointer",
              textDecoration: "none"
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.textDecoration = "underline";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.textDecoration = "none";
            }}
          >
            Create one
          </button>
        </div>
      </div>
    </div>
  );
}
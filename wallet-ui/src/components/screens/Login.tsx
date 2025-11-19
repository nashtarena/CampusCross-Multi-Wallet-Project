import React, { useState } from 'react';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { useAppContext } from '../../App';
import { authApi } from '../../services/walletApi';
import { toast } from 'sonner';
import { Wallet, Lock } from 'lucide-react';

interface LoginProps {
  onNext: () => void;
}

export function Login({ onNext }: LoginProps) {
  const [showPasswordLogin, setShowPasswordLogin] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [password, setPassword] = useState('');
  const [securityAnswer, setSecurityAnswer] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [studentId, setStudentId] = useState('');
  const { userPassword } = useAppContext();

  const handlePasswordLogin = async () => {
    if (!studentId || !password) {
      toast.error('Please enter both student ID and password');
      return;
    }

    setIsLoading(true);
    
    try {
      const response = await authApi.login(studentId, password);
      
      // Store token and user data
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        email: response.email,
        fullName: response.fullName,
        role: response.role,
        status: response.status,
        kycStatus: response.kycStatus || 'NOT_STARTED'
      }));
      
      toast.success('Login successful!');
      setShowPasswordLogin(false);
      onNext();
    } catch (error) {
      console.error('Login error:', error);
      toast.error(error instanceof Error ? error.message : 'Login failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = () => {
    // Mock security question: "What is your favorite color?"
    // Expected answer: "blue"
    if (securityAnswer.toLowerCase() === 'blue') {
      toast.success(`Your password is: ${userPassword || 'Not set yet'}`);
      setShowForgotPassword(false);
    } else {
      toast.error('Incorrect answer to security question');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-indigo-900 to-purple-900 flex flex-col items-center justify-center p-6 relative overflow-hidden">
      {/* Animated Background Elements */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-indigo-500/20 rounded-full blur-3xl animate-pulse" />
        <div className="absolute top-60 -left-20 w-60 h-60 bg-purple-500/20 rounded-full blur-3xl animate-pulse delay-1000" />
        <div className="absolute -bottom-20 right-20 w-72 h-72 bg-blue-500/20 rounded-full blur-3xl animate-pulse delay-500" />
      </div>

      <div className="relative z-10 flex flex-col items-center">
        <div className="flex items-center gap-2 mb-12">
          <div className="w-12 h-12 rounded-xl bg-white flex items-center justify-center">
            <Wallet className="text-indigo-600" size={28} />
          </div>
          <h1 className="text-3xl text-white">CampusCross</h1>
        </div>

        <div className="flex flex-col items-center">
          <div className="w-32 h-32 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center mb-8">
            <Lock className="text-white" size={64} />
          </div>
          
          <h2 className="text-white text-2xl mb-2">Welcome Back</h2>
          <p className="text-white/80 text-center mb-12">
            Enter your password to sign in
          </p>

          <Button 
            onClick={() => setShowPasswordLogin(true)}
            className="w-64 bg-white text-indigo-600 hover:bg-white/90 rounded-xl h-12 mb-4"
          >
            <Lock size={20} className="mr-2" />
            Use Password
          </Button>

          <Button 
            onClick={() => setShowForgotPassword(true)}
            variant="ghost"
            className="text-white hover:bg-white/10 rounded-xl"
          >
            Forgot Password?
          </Button>
        </div>

        <div className="absolute bottom-8 text-center">
          <p className="text-white/60 text-xs">
            Secured with 256-bit encryption
          </p>
        </div>
      </div>

      {/* Password Login Dialog */}
      <Dialog open={showPasswordLogin} onOpenChange={setShowPasswordLogin}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Enter Login Details</DialogTitle>
            <DialogDescription>
              Please enter your student ID and password to continue
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="studentId">Student ID</Label>
              <Input
                id="studentId"
                type="text"
                placeholder="Enter your student ID"
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                className="h-12 rounded-xl"
                onKeyDown={(e) => e.key === 'Enter' && handlePasswordLogin()}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="h-12 rounded-xl"
                onKeyDown={(e) => e.key === 'Enter' && handlePasswordLogin()}
              />
            </div>
            <Button 
              onClick={handlePasswordLogin}
              disabled={isLoading}
              className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600"
            >
              {isLoading ? 'Signing In...' : 'Sign In'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Forgot Password Dialog */}
      <Dialog open={showForgotPassword} onOpenChange={setShowForgotPassword}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Forgot Password</DialogTitle>
            <DialogDescription>
              Answer the security question to retrieve your password
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="security-question">What is your favorite color?</Label>
              <Input
                id="security-question"
                type="text"
                placeholder="Enter your answer"
                value={securityAnswer}
                onChange={(e) => setSecurityAnswer(e.target.value)}
                className="h-12 rounded-xl"
                onKeyDown={(e) => e.key === 'Enter' && handleForgotPassword()}
              />
              <p className="text-xs text-gray-500">Hint: A primary color starting with 'b'</p>
            </div>
            <Button 
              onClick={handleForgotPassword}
              className="w-full h-12 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600"
            >
              Retrieve Password
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

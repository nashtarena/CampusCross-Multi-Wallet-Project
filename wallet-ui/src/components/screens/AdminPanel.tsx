import React, { useState, useEffect } from "react";
import { Button } from "../ui/button";
import { Card } from "../ui/card";
import { Input } from "../ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../ui/tabs";
import {
  ArrowLeft,
  Users,
  FileCheck,
  Activity,
  Download,
  Upload,
  CheckCircle2,
  AlertCircle,
  Clock,
  LogOut,
  FileText,
  X
} from "lucide-react";
import { Badge } from "../ui/badge";
import { userApi, walletApi } from "../../services/walletApi";
import { toast } from "sonner";

interface CollegeUser {
  userId?: string;      // backend returns userId (studentId)
  fullName: string;
  studentId?: string;
  email?: string;
  kycStatus?: string;
}

interface DisbursementRow {
  studentId: string;
  studentName: string;
  amount: number;
  currency: string;
  status: 'pending' | 'success' | 'error';
  error?: string;
}

export function AdminPanel() {
  const [activeTab, setActiveTab] = useState("users");
  const [collegeUsers, setCollegeUsers] = useState<CollegeUser[]>([]);
  const [adminCollege, setAdminCollege] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [disbursementData, setDisbursementData] = useState<DisbursementRow[]>([]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [processingIndex, setProcessingIndex] = useState(-1);

  useEffect(() => {
    const fetchCollegeUsers = async () => {
      setIsLoading(true);
      setError("");

      try {
        const userStr = localStorage.getItem("user");
        if (!userStr) {
          setError("No user data found. Please log in again.");
          setIsLoading(false);
          return;
        }

        const user = JSON.parse(userStr);

        if (user.role !== "ADMIN") {
          setError("You must be an admin to access this panel");
          setIsLoading(false);
          return;
        }

        // fetch profile from backend (gives campusName and userId)
        const userProfile = await userApi.getCurrentUserProfile();
        const campusName = userProfile.campusName || userProfile.collegeName || userProfile.college || userProfile.campus;

        if (!campusName) {
          setError(`No campus information found in your profile. Available fields: ${Object.keys(userProfile).join(', ')}`);
          setIsLoading(false);
          return;
        }

        setAdminCollege(campusName);

        // fetch users by college (backend returns an array of user responses)
        const users = await userApi.getUsersByCollege(campusName);
        // Map robustly to handle field names from backend (userId vs studentId)
        const mapped: CollegeUser[] = users.map((u: any) => ({
          userId: u.userId || u.studentId || u.id || undefined,
          fullName: u.fullName || `${u.firstName || ''} ${u.lastName || ''}`.trim() || 'Unknown',
          studentId: u.studentId || u.userId || u.id,
          email: u.email,
          kycStatus: u.kycStatus || u.kyc || 'NOT_STARTED',
        }));

        setCollegeUsers(mapped);

        // If disbursement CSV was already loaded earlier, reattach student names
        setDisbursementData(prev => prev.map(row => {
          const st = mapped.find(mu => mu.studentId === row.studentId || mu.userId === row.studentId);
          return { ...row, studentName: st?.fullName || row.studentName };
        }));

      } catch (err: any) {
        console.error("Failed to fetch college users:", err);
        setError(err?.message || String(err) || 'Failed to load data');
      } finally {
        setIsLoading(false);
      }
    };

    fetchCollegeUsers();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("user");
    window.dispatchEvent(new CustomEvent("navigate", { detail: { screen: "login" } }));
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.name.endsWith('.csv')) {
      toast.error('Please upload a CSV file');
      return;
    }

    setCsvFile(file);
    const reader = new FileReader();

    reader.onload = (event) => {
      try {
        const text = event.target?.result as string;
        const rows = text.split('\n').map(r => r.trim()).filter(r => r);
        const dataRows = rows.slice(1); // skip header

        const parsedData: DisbursementRow[] = dataRows.map(row => {
          const [studentIdRaw, amountRaw, currencyRaw] = row.split(',').map(cell => cell?.trim());
          const studentId = studentIdRaw;
          const amount = parseFloat(amountRaw || '0');
          const currency = (currencyRaw || 'USD').toUpperCase();

          const student = collegeUsers.find(u => u.studentId === studentId || u.userId === studentId);

          return {
            studentId,
            studentName: student?.fullName || 'Unknown',
            amount,
            currency,
            status: 'pending'
          };
        }).filter(r => r.studentId && !isNaN(r.amount) && r.amount > 0);

        if (parsedData.length === 0) {
          toast.error('No valid data found in CSV');
          return;
        }

        setDisbursementData(parsedData);
        toast.success(`Loaded ${parsedData.length} disbursements`);
      } catch (err) {
        console.error('CSV parsing error:', err);
        toast.error('Failed to parse CSV file');
      }
    };

    reader.readAsText(file);
  };

  const handleProcessDisbursement = async () => {
    if (disbursementData.length === 0) {
      toast.error('No disbursement data loaded');
      return;
    }

    setIsProcessing(true);
    const updated = [...disbursementData];

    for (let i = 0; i < updated.length; i++) {
      setProcessingIndex(i);
      const row = updated[i];

      try {
        // Locate student in loaded users to get the userId/studentId (backend uses studentId as userId)
        const student = collegeUsers.find(u => u.studentId === row.studentId || u.userId === row.studentId);
        const userId = student?.userId || student?.studentId || row.studentId;

        if (!userId) throw new Error('Student user id not found');

        // 1) Get student's default wallet (backend: GET /api/wallets/user/{userId}/default)
        const wallet = await walletApi.getDefaultWallet(userId);

        if (!wallet || !wallet.id) {
          throw new Error('Student has no default wallet');
        }

        // 2) Add funds to wallet (backend: POST /api/wallets/{walletId}/add-funds)
        await walletApi.addFunds(wallet.id, row.amount);

        updated[i] = { ...row, status: 'success', error: undefined };
        toast.success(`✓ Sent ${row.currency} ${row.amount} to ${row.studentName}`);
      } catch (err: any) {
        console.error(`Disbursement failed for ${row.studentId}:`, err);
        updated[i] = {
          ...row,
          status: 'error',
          error: err?.message || String(err) || 'Failed to disburse'
        };
        toast.error(`✗ Failed: ${row.studentName} — ${updated[i].error}`);
      }

      setDisbursementData([...updated]);

      // small delay to avoid hammering backend
      await new Promise(resolve => setTimeout(resolve, 400));
    }

    setIsProcessing(false);
    setProcessingIndex(-1);

    const successCount = updated.filter(d => d.status === 'success').length;
    const failCount = updated.filter(d => d.status === 'error').length;
    toast.success(`Completed: ${successCount} successful, ${failCount} failed`);
  };

  const clearDisbursement = () => {
    setCsvFile(null);
    setDisbursementData([]);
  };

  const totalAmount = disbursementData.reduce((sum, row) => sum + (row.amount || 0), 0);
  const processingFee = totalAmount * 0.01; // 1% fee

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800">
      {/* Header */}
      <div className="bg-[#263238] p-6 pb-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-xl text-white">Admin Panel</h1>
            <p className="text-sm text-white/60">Administrative controls</p>
          </div>

          <Button
            variant="outline"
            className="bg-black/10 border-white/20 text-white flex items-center gap-2"
            onClick={handleLogout}
          >
            <LogOut size={16} />
            Logout
          </Button>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-3">
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <Users className="text-blue-400 mb-2" size={24} />
            <p className="text-xs text-white/60">Active Users</p>
            <p className="text-2xl font-bold text-white mt-1">{collegeUsers.length}</p>
          </Card>
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <Activity className="text-purple-400 mb-2" size={24} />
            <p className="text-xs text-white/60">Volume Today</p>
            <p className="text-2xl font-bold text-white mt-1">$0</p>
          </Card>
        </div>
      </div>

      <div className="px-6 -mt-4">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="w-full bg-white/5 backdrop-blur-sm border border-white/10 rounded-xl p-1 mb-6">
            <TabsTrigger value="users" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">Users</TabsTrigger>
            <TabsTrigger value="bulk" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">Bulk</TabsTrigger>
          </TabsList>

          <TabsContent value="users" className="mt-0 space-y-4">
            <Card className="p-6 bg-white/5 backdrop-blur-sm border-white/10">
              <h3 className="text-white mb-4">College Users</h3>

              <div className="bg-white/5 rounded-xl p-4 mb-4">
                <p className="text-sm text-white/60 mb-1">Your Institution</p>
                <p className="text-white font-medium">{isLoading ? "Loading..." : adminCollege || "No college found"}</p>
              </div>

              {error && (
                <div className="bg-red-500/20 border border-red-500/30 rounded-xl p-4 mb-4">
                  <p className="text-red-400 text-sm">{error}</p>
                </div>
              )}

              {isLoading ? (
                <div className="text-center py-8">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white mx-auto mb-3"></div>
                  <p className="text-white/60">Loading users...</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {collegeUsers.length > 0 ? (
                    collegeUsers.map((user: CollegeUser, index: number) => (
                      <Card key={index} className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-blue-500/20 flex items-center justify-center">
                              <Users size={20} className="text-blue-400" />
                            </div>
                            <div>
                              <p className="text-white font-medium">{user.fullName}</p>
                              <p className="text-xs text-white/60">{user.studentId || user.userId}</p>
                            </div>
                          </div>
                          <div className="text-right">
                            <Badge className="bg-green-500/20 text-green-400 border-0">{user.kycStatus}</Badge>
                            <p className="text-xs text-white/60 mt-1">{user.email}</p>
                          </div>
                        </div>
                      </Card>
                    ))
                  ) : (
                    <div className="text-center py-8">
                      <Users className="text-white/20 mx-auto mb-3" size={48} />
                      <p className="text-white/60">No users found from your institution</p>
                    </div>
                  )}
                </div>
              )}
            </Card>
          </TabsContent>

          <TabsContent value="bulk" className="mt-0 space-y-4">
            <Card className="p-6 bg-white/5 backdrop-blur-sm border-white/10">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-white">Bulk Disbursement</h3>
                {disbursementData.length > 0 && (
                  <Button variant="ghost" size="sm" onClick={clearDisbursement} className="text-white/60 hover:text-white">
                    <X size={16} className="mr-1" /> Clear
                  </Button>
                )}
              </div>

              <div className="space-y-4">
                {disbursementData.length === 0 ? (
                  <div className="border-2 border-dashed border-white/20 rounded-xl p-8 text-center">
                    <Upload className="text-white/40 mx-auto mb-3" size={48} />
                    <p className="text-white mb-2">Upload CSV File</p>
                    <p className="text-xs text-white/60 mb-4">Format: Student ID, Amount, Currency<br/>Example: STU001, 100, USD</p>
                    <input type="file" accept=".csv" onChange={handleFileUpload} className="hidden" id="csv-upload" />
                    <label htmlFor="csv-upload">
                      <Button variant="outline" className="border-white/20 text-white bg-black/20" asChild>
                        <span>Choose File</span>
                      </Button>
                    </label>
                  </div>
                ) : (
                  <>
                    <div className="bg-white/5 rounded-xl p-4 max-h-64 overflow-y-auto">
                      <div className="space-y-2">
                        {disbursementData.map((row, index) => (
                          <div key={index} className={`flex items-center justify-between p-3 rounded-lg ${
                              processingIndex === index ? 'bg-blue-500/20'
                              : row.status === 'success' ? 'bg-green-500/10'
                              : row.status === 'error' ? 'bg-red-500/10'
                              : 'bg-white/5'
                            }`}
                          >
                            <div className="flex items-center gap-3">
                              {row.status === 'success' ? (
                                <CheckCircle2 size={20} className="text-green-400" />
                              ) : row.status === 'error' ? (
                                <AlertCircle size={20} className="text-red-400" />
                              ) : (
                                <FileText size={20} className="text-white/40" />
                              )}
                              <div>
                                <p className="text-white text-sm font-medium">{row.studentName}</p>
                                <p className="text-white/60 text-xs">{row.studentId}</p>
                              </div>
                            </div>
                            <div className="text-right">
                              <p className="text-white font-medium">{row.currency} {row.amount.toFixed(2)}</p>
                              {row.error && <p className="text-red-400 text-xs">{row.error}</p>}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div className="bg-white/5 rounded-xl p-4">
                      <p className="text-sm text-white mb-2">Disbursement Summary</p>
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span className="text-white/60">Total Recipients</span>
                          <span className="text-white">{disbursementData.length}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-white/60">Total Amount</span>
                          <span className="text-white">${totalAmount.toFixed(2)}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-white/60">Processing Fee (1%)</span>
                          <span className="text-white">${processingFee.toFixed(2)}</span>
                        </div>
                        <div className="h-px bg-white/10 my-2"></div>
                        <div className="flex justify-between text-base font-medium">
                          <span className="text-white">Total</span>
                          <span className="text-white">${(totalAmount + processingFee).toFixed(2)}</span>
                        </div>
                      </div>
                    </div>

                    <Button className="w-full h-12 rounded-xl" style={{ background: "#263238" }} onClick={handleProcessDisbursement} disabled={isProcessing}>
                      {isProcessing ? `Processing... (${processingIndex + 1}/${disbursementData.length})` : 'Process Disbursement'}
                    </Button>
                  </>
                )}
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

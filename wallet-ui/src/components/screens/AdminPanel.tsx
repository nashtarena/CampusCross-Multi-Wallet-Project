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
} from "lucide-react";
import { Badge } from "../ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../ui/table";
import { userApi } from "../../services/walletApi";

interface AdminPanelProps {
  onBack: () => void;
}

interface CollegeUser {
  fullName: string;
  studentId: string;
  email: string;
  kycStatus: string;
}

export function AdminPanel({ onBack }: AdminPanelProps) {
  const [activeTab, setActiveTab] = useState("users");
  const [collegeUsers, setCollegeUsers] = useState<CollegeUser[]>([]);
  const [adminCollege, setAdminCollege] = useState("");

  useEffect(() => {
    const fetchCollegeUsers = async () => {
      const userStr = localStorage.getItem("user");
      if (userStr) {
        const user = JSON.parse(userStr);
        setAdminCollege(user.campusName);

        try {
          const users = await userApi.getUsersByCollege(user.campusName);
          setCollegeUsers(
            users.map((u: any) => ({
              fullName: u.fullName,
              studentId: u.studentId,
              email: u.email,
              kycStatus: u.kycStatus,
            }))
          );
        } catch (error) {
          console.error("Failed to fetch college users:", error);
        }
      }
    };

    fetchCollegeUsers();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("user");

    window.dispatchEvent(
      new CustomEvent("navigate", {
        detail: { screen: "login" },
      })
    );
  };

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
            className="bg-black/10 border-black/20 text-white flex items-center gap-2"
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
          </Card>
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <Activity className="text-purple-400 mb-2" size={24} />
            <p className="text-xs text-white/60">Volume Today</p>
          </Card>
        </div>
      </div>

      <div className="px-6 -mt-4">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="w-full bg-white/5 backdrop-blur-sm border border-white/10 rounded-xl p-1 mb-6">
            <TabsTrigger
              value="users"
              className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60"
            >
              Users
            </TabsTrigger>
            <TabsTrigger
              value="bulk"
              className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60"
            >
              Bulk
            </TabsTrigger>
          </TabsList>

          <TabsContent value="users" className="mt-0 space-y-4">
            <Card className="p-6 bg-white/5 backdrop-blur-sm border-white/10">
              <h3 className="text-white mb-4">College Users</h3>

              <div className="bg-white/5 rounded-xl p-4 mb-4">
                <p className="text-sm text-white/60 mb-1">Your Institution</p>
                <p className="text-white font-medium">
                  {adminCollege || "Loading..."}
                </p>
              </div>

              <div className="space-y-3">
                {collegeUsers.length > 0 ? (
                  collegeUsers.map((user: CollegeUser, index: number) => (
                    <Card
                      key={index}
                      className="p-4 bg-white/5 backdrop-blur-sm border-white/10"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-blue-500/20 flex items-center justify-center">
                            <Users size={20} className="text-blue-400" />
                          </div>
                          <div>
                            <p className="text-white font-medium">
                              {user.fullName}
                            </p>
                            <p className="text-xs text-white/60">
                              {user.studentId}
                            </p>
                          </div>
                        </div>
                        <div className="text-right">
                          <Badge className="bg-green-500/20 text-green-400">
                            {user.kycStatus || "NOT_STARTED"}
                          </Badge>
                          <p className="text-xs text-white/60 mt-1">
                            {user.email}
                          </p>
                        </div>
                      </div>
                    </Card>
                  ))
                ) : (
                  <div className="text-center py-8">
                    <Users className="text-white/20 mx-auto mb-3" size={48} />
                    <p className="text-white/60">
                      No users found from your institution
                    </p>
                  </div>
                )}
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="bulk" className="mt-0 space-y-4">
            <Card className="p-6 bg-white/5 backdrop-blur-sm border-white/10">
              <h3 className="text-white mb-4">Bulk Disbursement</h3>

              <div className="space-y-4">
                <div className="border-2 border-dashed border-white/20 rounded-xl p-8 text-center">
                  <Upload className="text-white/40 mx-auto mb-3" size={48} />
                  <p className="text-white mb-2">Upload CSV File</p>
                  <p className="text-xs text-white/60 mb-4">
                    File should contain: Student ID, Amount, Currency
                  </p>
                  <Button
                    variant="outline"
                    className="border-white/20 text-white hover:bg-white/10"
                  >
                    Choose File
                  </Button>
                </div>

                <div className="bg-white/5 rounded-xl p-4">
                  <p className="text-sm text-white mb-2">
                    Disbursement Summary
                  </p>
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-white/60">Total Recipients</span>
                      <span className="text-white">0</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-white/60">Total Amount</span>
                      <span className="text-white">$0.00</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-white/60">Processing Fee</span>
                      <span className="text-white">$0.00</span>
                    </div>
                  </div>
                </div>

                <Button
                  className="w-full h-12 rounded-xl"
                  style={{ background: "#263238" }}
                  disabled
                >
                  Process Disbursement
                </Button>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

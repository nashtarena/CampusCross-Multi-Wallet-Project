import React, { useState, useEffect } from 'react';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Input } from '../ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { ArrowLeft, Users, FileCheck, Activity, Download, Upload, CheckCircle2, AlertCircle, Clock } from 'lucide-react';
import { Badge } from '../ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table';

interface AdminPanelProps {
  onBack: () => void;
}

const kycRequests = [
  { id: 1, name: 'Alice Brown', studentId: 'STU123456', tier: 'Tier 1', status: 'pending', date: '2024-11-09' },
  { id: 2, name: 'Bob Wilson', studentId: 'STU789012', tier: 'Tier 2', status: 'approved', date: '2024-11-08' },
  { id: 3, name: 'Carol Davis', studentId: 'STU345678', tier: 'Tier 1', status: 'rejected', date: '2024-11-07' }
];

const activityLogs = [
  { id: 1, action: 'Bulk disbursement', user: 'Admin', amount: 50000, currency: 'USD', timestamp: '2024-11-09 14:30' },
  { id: 2, action: 'KYC approval', user: 'Admin', details: 'STU789012', timestamp: '2024-11-09 12:15' },
  { id: 3, action: 'System config', user: 'SuperAdmin', details: 'Rate limits updated', timestamp: '2024-11-09 10:00' }
];

interface CollegeUser {
  fullName: string;
  studentId: string;
  email: string;
  kycStatus: string;
}

export function AdminPanel({ onBack }: AdminPanelProps) {
  const [activeTab, setActiveTab] = useState('users');
  const [collegeUsers, setCollegeUsers] = useState<CollegeUser[]>([]);
  const [adminCollege, setAdminCollege] = useState('');

  useEffect(() => {
    // Get admin's college from localStorage
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      setAdminCollege('Default University'); // Using hardcoded value since campusName is not in AuthResponse
      
      // Mock data for demonstration - in real app, this would be an API call
      // to fetch users from the same college
      const mockCollegeUsers = [
        {
          fullName: 'Alice Johnson',
          studentId: 'STU001',
          email: 'alice@college.edu',
          kycStatus: 'COMPLETED'
        },
        {
          fullName: 'Bob Smith',
          studentId: 'STU002', 
          email: 'bob@college.edu',
          kycStatus: 'PENDING'
        },
        {
          fullName: 'Carol Williams',
          studentId: 'STU003',
          email: 'carol@college.edu', 
          kycStatus: 'NOT_STARTED'
        }
      ];
      
      setCollegeUsers(mockCollegeUsers);
    }
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800">
      {/* Header */}
      <div className="bg-[#263238] p-6 pb-8">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={onBack} className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <div>
            <h1 className="text-xl text-white">Admin Panel</h1>
            <p className="text-sm text-white/60">Administrative controls</p>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-3 gap-3">
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <Users className="text-blue-400 mb-2" size={24} />
            <p className="text-2xl text-white mb-1">2,543</p>
            <p className="text-xs text-white/60">Active Users</p>
          </Card>
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <FileCheck className="text-green-400 mb-2" size={24} />
            <p className="text-2xl text-white mb-1">128</p>
            <p className="text-xs text-white/60">Pending KYC</p>
          </Card>
          <Card className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
            <Activity className="text-purple-400 mb-2" size={24} />
            <p className="text-2xl text-white mb-1">$2.5M</p>
            <p className="text-xs text-white/60">Volume Today</p>
          </Card>
        </div>
      </div>

      <div className="px-6 -mt-4">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="w-full bg-white/5 backdrop-blur-sm border border-white/10 rounded-xl p-1 mb-6">
            <TabsTrigger value="users" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">
              Users
            </TabsTrigger>
            <TabsTrigger value="bulk" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">
              Bulk
            </TabsTrigger>
            <TabsTrigger value="kyc" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">
              KYC
            </TabsTrigger>
            <TabsTrigger value="logs" className="flex-1 rounded-lg data-[state=active]:bg-white/10 data-[state=active]:text-white text-white/60">
              Logs
            </TabsTrigger>
          </TabsList>

          <TabsContent value="users" className="mt-0 space-y-4">
            <Card className="p-6 bg-white/5 backdrop-blur-sm border-white/10">
              <h3 className="text-white mb-4">College Users</h3>
              
              <div className="bg-white/5 rounded-xl p-4 mb-4">
                <p className="text-sm text-white/60 mb-1">Your Institution</p>
                <p className="text-white font-medium">
                  {adminCollege || 'Loading...'}
                </p>
              </div>

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
                            <p className="text-xs text-white/60">{user.studentId}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <Badge className="bg-green-500/20 text-green-400">
                            {user.kycStatus || 'NOT_STARTED'}
                          </Badge>
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
                  <Button variant="outline" className="border-white/20 text-white hover:bg-white/10">
                    Choose File
                  </Button>
                </div>

                <div className="bg-white/5 rounded-xl p-4">
                  <p className="text-sm text-white mb-2">Disbursement Summary</p>
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
                  style={{ background: '#263238' }}
                  disabled
                >
                  Process Disbursement
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="kyc" className="mt-0 space-y-3">
            {kycRequests.map((request) => (
              <Card key={request.id} className="p-4 bg-white/5 backdrop-blur-sm border-white/10">
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <p className="text-white">{request.name}</p>
                    <p className="text-xs text-white/60">{request.studentId} • {request.tier}</p>
                  </div>
                  <Badge 
                    variant={
                      request.status === 'approved' ? 'default' : 
                      request.status === 'pending' ? 'secondary' : 
                      'destructive'
                    }
                    className={
                      request.status === 'approved' ? 'bg-green-500/20 text-green-400' :
                      request.status === 'pending' ? 'bg-amber-500/20 text-amber-400' :
                      'bg-red-500/20 text-red-400'
                    }
                  >
                    {request.status}
                  </Badge>
                </div>
                
                {request.status === 'pending' && (
                  <div className="flex gap-2">
                    <Button size="sm" className="flex-1 bg-green-500/20 text-green-400 hover:bg-green-500/30">
                      <CheckCircle2 size={14} className="mr-1" />
                      Approve
                    </Button>
                    <Button size="sm" variant="outline" className="flex-1 border-red-500/20 text-red-400 hover:bg-red-500/10">
                      Reject
                    </Button>
                  </div>
                )}
              </Card>
            ))}
          </TabsContent>

          <TabsContent value="logs" className="mt-0">
            <Card className="bg-white/5 backdrop-blur-sm border-white/10 overflow-hidden">
              <div className="p-4 border-b border-white/10 flex items-center justify-between">
                <h3 className="text-white">Activity Logs</h3>
                <Button size="sm" variant="outline" className="border-white/20 text-white hover:bg-white/10">
                  <Download size={14} className="mr-2" />
                  Export
                </Button>
              </div>
              
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow className="border-white/10 hover:bg-transparent">
                      <TableHead className="text-white/60">Action</TableHead>
                      <TableHead className="text-white/60">User</TableHead>
                      <TableHead className="text-white/60">Details</TableHead>
                      <TableHead className="text-white/60">Time</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {activityLogs.map((log) => (
                      <TableRow key={log.id} className="border-white/10 hover:bg-white/5">
                        <TableCell className="text-white">{log.action}</TableCell>
                        <TableCell className="text-white/80">{log.user}</TableCell>
                        <TableCell className="text-white/60">
                          {log.amount ? `${log.amount} ${log.currency}` : log.details}
                        </TableCell>
                        <TableCell className="text-white/60 text-xs">{log.timestamp}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}

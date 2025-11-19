import React, { useState, useEffect } from 'react';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { Switch } from '../ui/switch';
import { Label } from '../ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '../ui/dialog';
import { Badge } from '../ui/badge';
import { Separator } from '../ui/separator';
import { toast } from 'sonner';
import { notificationsApi, Notification, NotificationPreference } from '../../services/notificationsApi';
import { 
  Bell, 
  BellOff, 
  Check, 
  CheckCheck, 
  Trash2, 
  Settings, 
  X,
  Mail,
  Smartphone,
  Volume2
} from 'lucide-react';

interface NotificationsPanelProps {
  isOpen: boolean;
  onClose: () => void;
  userId: string;
}

export function NotificationsPanel({ isOpen, onClose, userId }: NotificationsPanelProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [preferences, setPreferences] = useState<NotificationPreference | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showSettings, setShowSettings] = useState(false);

  useEffect(() => {
    if (isOpen && userId) {
      fetchNotifications();
      fetchPreferences();
    }
  }, [isOpen, userId]);

  const fetchNotifications = async () => {
    try {
      setIsLoading(true);
      const userNotifications = await notificationsApi.getUserNotifications(userId);
      setNotifications(userNotifications);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchPreferences = async () => {
    try {
      const userPreferences = await notificationsApi.getNotificationPreferences(userId);
      setPreferences(userPreferences);
    } catch (error) {
      console.error('Failed to fetch preferences:', error);
    }
  };

  const markAsRead = async (notificationId: string) => {
    try {
      await notificationsApi.markNotificationAsRead(notificationId);
      setNotifications(prev => 
        prev.map(n => n.id === notificationId ? { ...n, isRead: true } : n)
      );
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationsApi.markAllNotificationsAsRead(userId);
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      toast.success('All notifications marked as read');
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  const deleteNotification = async (notificationId: string) => {
    try {
      await notificationsApi.deleteNotification(notificationId);
      setNotifications(prev => prev.filter(n => n.id !== notificationId));
      toast.success('Notification deleted');
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  const updatePreferences = async (newPreferences: NotificationPreference) => {
    try {
      await notificationsApi.updateNotificationPreferences(newPreferences);
      setPreferences(newPreferences);
    } catch (error) {
      console.error('Failed to update preferences:', error);
    }
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const handleNotificationClick = (notification: Notification) => {
    if (!notification.isRead) {
      markAsRead(notification.id);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-md max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bell className="h-5 w-5" />
              <DialogTitle>Notifications</DialogTitle>
              {unreadCount > 0 && (
                <Badge variant="secondary" className="ml-2">
                  {unreadCount} new
                </Badge>
              )}
            </div>
            <div className="flex items-center gap-1">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowSettings(true)}
                className="h-8 w-8 p-0"
              >
                <Settings className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </DialogHeader>

        {isLoading ? (
          <div className="flex-1 flex items-center justify-center py-8">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2"></div>
              <p className="text-sm text-muted-foreground">Loading notifications...</p>
            </div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex-1 flex items-center justify-center py-8">
            <div className="text-center">
              <BellOff className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">No notifications yet</p>
              <p className="text-xs text-muted-foreground mt-1">We'll notify you when something important happens</p>
            </div>
          </div>
        ) : (
          <>
            {unreadCount > 0 && (
              <div className="px-1 pb-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={markAllAsRead}
                  className="w-full justify-start text-sm"
                >
                  <CheckCheck className="h-4 w-4 mr-2" />
                  Mark all as read
                </Button>
              </div>
            )}

            <div className="flex-1 overflow-y-auto px-1">
              <div className="space-y-2">
                {notifications.map((notification) => {
                  const style = notificationsApi.getNotificationStyle(notification.type);
                  return (
                    <Card
                      key={notification.id}
                      className={`p-3 cursor-pointer transition-colors ${
                        notification.isRead 
                          ? 'bg-background hover:bg-muted/50' 
                          : 'bg-muted border-primary/20'
                      }`}
                      onClick={() => handleNotificationClick(notification)}
                    >
                      <div className="flex items-start gap-3">
                        <div className={`w-8 h-8 rounded-full flex items-center justify-center ${style.bgColor}`}>
                          <span className="text-sm">{style.icon}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-start justify-between gap-2">
                            <div className="flex-1">
                              <p className={`text-sm font-medium ${notification.isRead ? 'text-foreground' : 'text-foreground'}`}>
                                {notification.title}
                              </p>
                              <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                                {notification.message}
                              </p>
                              <p className="text-xs text-muted-foreground mt-1">
                                {notificationsApi.formatNotificationTime(notification.createdAt)}
                              </p>
                            </div>
                            <div className="flex items-center gap-1">
                              {!notification.isRead && (
                                <div className="w-2 h-2 rounded-full bg-primary"></div>
                              )}
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={(e: React.MouseEvent) => {
                                  e.stopPropagation();
                                  deleteNotification(notification.id);
                                }}
                                className="h-6 w-6 p-0 opacity-50 hover:opacity-100"
                              >
                                <Trash2 className="h-3 w-3" />
                              </Button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </Card>
                  );
                })}
              </div>
            </div>
          </>
        )}

        {/* Notification Settings Dialog */}
        <Dialog open={showSettings} onOpenChange={setShowSettings}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Notification Settings</DialogTitle>
              <DialogDescription>
                Choose what notifications you want to receive
              </DialogDescription>
            </DialogHeader>

            {preferences && (
              <div className="space-y-4 py-4">
                <div className="space-y-3">
                  <Label className="text-sm font-medium">Notification Types</Label>
                  
                  <div className="flex items-center justify-between">
                    <Label htmlFor="transactions" className="text-sm">Transactions</Label>
                    <Switch
                      id="transactions"
                      checked={preferences.transactionNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, transactionNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="wallet" className="text-sm">Wallet Updates</Label>
                    <Switch
                      id="wallet"
                      checked={preferences.walletNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, walletNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="kyc" className="text-sm">KYC Status</Label>
                    <Switch
                      id="kyc"
                      checked={preferences.kycNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, kycNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="alerts" className="text-sm">Rate Alerts</Label>
                    <Switch
                      id="alerts"
                      checked={preferences.rateAlertNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, rateAlertNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="security" className="text-sm">Security</Label>
                    <Switch
                      id="security"
                      checked={preferences.securityNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, securityNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="system" className="text-sm">System Updates</Label>
                    <Switch
                      id="system"
                      checked={preferences.systemNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, systemNotifications: checked })
                      }
                    />
                  </div>
                </div>

                <Separator />

                <div className="space-y-3">
                  <Label className="text-sm font-medium">Delivery Methods</Label>
                  
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Mail className="h-4 w-4" />
                      <Label htmlFor="email" className="text-sm">Email</Label>
                    </div>
                    <Switch
                      id="email"
                      checked={preferences.emailNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, emailNotifications: checked })
                      }
                    />
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Smartphone className="h-4 w-4" />
                      <Label htmlFor="push" className="text-sm">Push Notifications</Label>
                    </div>
                    <Switch
                      id="push"
                      checked={preferences.pushNotifications}
                      onCheckedChange={(checked: boolean) =>
                        updatePreferences({ ...preferences, pushNotifications: checked })
                      }
                    />
                  </div>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </DialogContent>
    </Dialog>
  );
}

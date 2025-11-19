import { toast } from 'sonner';

export interface Notification {
  id: string;
  userId: string;
  type: 'TRANSACTION' | 'WALLET' | 'KYC' | 'RATE_ALERT' | 'SECURITY' | 'SYSTEM';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  data?: any; // Additional data related to the notification
}

export interface NotificationPreference {
  userId: string;
  transactionNotifications: boolean;
  walletNotifications: boolean;
  kycNotifications: boolean;
  rateAlertNotifications: boolean;
  securityNotifications: boolean;
  systemNotifications: boolean;
  emailNotifications: boolean;
  pushNotifications: boolean;
}

class NotificationsApi {
  private baseUrl = '/api/notifications';

  async getUserNotifications(userId: string): Promise<Notification[]> {
    try {
      const response = await fetch(`${this.baseUrl}/user/${userId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch notifications');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching notifications:', error);
      // Return empty array for production
      return [];
    }
  }

  async markNotificationAsRead(notificationId: string): Promise<void> {
    try {
      const response = await fetch(`${this.baseUrl}/${notificationId}/read`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to mark notification as read');
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
      toast.error('Failed to update notification');
    }
  }

  async markAllNotificationsAsRead(userId: string): Promise<void> {
    try {
      const response = await fetch(`${this.baseUrl}/user/${userId}/read-all`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to mark all notifications as read');
      }
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      toast.error('Failed to update notifications');
    }
  }

  async deleteNotification(notificationId: string): Promise<void> {
    try {
      const response = await fetch(`${this.baseUrl}/${notificationId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to delete notification');
      }
    } catch (error) {
      console.error('Error deleting notification:', error);
      toast.error('Failed to delete notification');
    }
  }

  async getNotificationPreferences(userId: string): Promise<NotificationPreference> {
    try {
      const response = await fetch(`${this.baseUrl}/preferences/${userId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch notification preferences');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching notification preferences:', error);
      // Return default preferences for production
      return {
        userId,
        transactionNotifications: true,
        walletNotifications: true,
        kycNotifications: true,
        rateAlertNotifications: true,
        securityNotifications: true,
        systemNotifications: true,
        emailNotifications: true,
        pushNotifications: true,
      };
    }
  }

  async updateNotificationPreferences(preferences: NotificationPreference): Promise<void> {
    try {
      const response = await fetch(`${this.baseUrl}/preferences`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(preferences),
      });

      if (!response.ok) {
        throw new Error('Failed to update notification preferences');
      }

      toast.success('Notification preferences updated');
    } catch (error) {
      console.error('Error updating notification preferences:', error);
      toast.error('Failed to update preferences');
    }
  }


  // Helper method to format notification time
  formatNotificationTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    const diffInDays = Math.floor(diffInHours / 24);

    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${diffInHours}h ago`;
    if (diffInDays === 1) return '1d ago';
    if (diffInDays < 7) return `${diffInDays}d ago`;
    return date.toLocaleDateString();
  }

  // Helper method to get notification icon and color
  getNotificationStyle(type: Notification['type']) {
    switch (type) {
      case 'TRANSACTION':
        return { icon: '💸', color: 'text-green-600', bgColor: 'bg-green-100 dark:bg-green-900' };
      case 'WALLET':
        return { icon: '💳', color: 'text-blue-600', bgColor: 'bg-blue-100 dark:bg-blue-900' };
      case 'KYC':
        return { icon: '📋', color: 'text-purple-600', bgColor: 'bg-purple-100 dark:bg-purple-900' };
      case 'RATE_ALERT':
        return { icon: '📈', color: 'text-amber-600', bgColor: 'bg-amber-100 dark:bg-amber-900' };
      case 'SECURITY':
        return { icon: '🔒', color: 'text-red-600', bgColor: 'bg-red-100 dark:bg-red-900' };
      case 'SYSTEM':
        return { icon: '⚙️', color: 'text-gray-600', bgColor: 'bg-gray-100 dark:bg-gray-900' };
      default:
        return { icon: '📢', color: 'text-gray-600', bgColor: 'bg-gray-100 dark:bg-gray-900' };
    }
  }
}

export const notificationsApi = new NotificationsApi();

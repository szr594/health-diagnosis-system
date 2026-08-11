import { AlertTriangle, Shield } from 'lucide-react';

interface RiskBadgeProps {
  level?: string;
  showIcon?: boolean;
  className?: string;
}

export default function RiskBadge({ level, showIcon = true, className = '' }: RiskBadgeProps) {
  const map: Record<string, { text: string; cls: string; icon: React.ElementType }> = {
    high: { text: '高风险', cls: 'badge-danger', icon: AlertTriangle },
    mid: { text: '中风险', cls: 'badge-warning', icon: AlertTriangle },
    low: { text: '低风险', cls: 'badge-success', icon: Shield },
  };

  const cfg = map[level || ''] || { text: '未知', cls: 'badge-info', icon: Shield };
  const Icon = cfg.icon;

  return (
    <span className={`badge ${cfg.cls} ${className}`}>
      {showIcon && <Icon className="h-3 w-3" />}
      {cfg.text}
    </span>
  );
}

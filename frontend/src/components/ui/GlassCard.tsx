import React from 'react';

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  onClick?: () => void;
  padding?: 'sm' | 'md' | 'lg';
}

export function GlassCard({ children, className = '', hover = false, onClick, padding = 'md' }: GlassCardProps) {
  const pad = padding === 'sm' ? 'p-4' : padding === 'lg' ? 'p-8' : 'p-6';
  return (
    <div
      onClick={onClick}
      className={`${hover ? 'card-hover' : 'card-base'} ${pad} ${className}`}
    >
      {children}
    </div>
  );
}

export function GlassPanel({ children, className = '', strong = false }: { children: React.ReactNode; className?: string; strong?: boolean }) {
  return (
    <div className={`${strong ? 'glass-panel-strong' : 'glass-panel'} p-6 ${className}`}>
      {children}
    </div>
  );
}

export function StatCard({
  icon: Icon,
  label,
  value,
  trend,
  color = 'brand',
  children,
}: {
  icon: React.ElementType;
  label: string;
  value: string | number;
  trend?: string;
  color?: 'brand' | 'accent' | 'warning';
  children?: React.ReactNode;
}) {
  const map = {
    brand: 'from-brand-400 to-brand-600 shadow-brand-500/25',
    accent: 'from-accent-400 to-accent-500 shadow-accent-500/25',
    warning: 'from-amber-400 to-amber-600 shadow-amber-500/25',
  };

  return (
    <div className="card-base flex items-center gap-4 p-5 transition-all duration-200 hover:-translate-y-1 hover:shadow-xl hover:shadow-brand-500/10">
      <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${map[color]} shadow-lg transition-transform duration-200 group-hover:scale-105`}>
        <Icon className="h-5 w-5 text-white" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs text-white/50">{label}</p>
        <div className="flex items-baseline gap-2">
          <p className="text-2xl font-bold text-white">{value}</p>
          {trend && <p className="text-xs text-white/40">{trend}</p>}
        </div>
        {children && <div className="mt-2">{children}</div>}
      </div>
    </div>
  );
}

export function MiniChart({ data, color = '#14b8a6' }: { data: number[]; color?: string }) {
  if (!data.length) return null;
  const mx = Math.max(...data, 1);
  const mn = Math.min(...data, 0);
  const rng = mx - mn || 1;
  const w = 80;
  const h = 32;
  const step = w / (data.length - 1 || 1);

  const pts = data.map((v, i) => {
    const x = i * step;
    const y = h - ((v - mn) / rng) * h;
    return `${x},${y}`;
  }).join(' ');

  return (
    <svg width={w} height={h} className="overflow-visible">
      <polyline
        fill="none"
        stroke={color}
        strokeWidth="2"
        points={pts}
        strokeLinecap="round"
        strokeLinejoin="round"
        className="drop-shadow-[0_0_6px_rgba(20,184,166,0.4)]"
      />
      {data.map((v, i) => (
        <circle
          key={i}
          cx={i * step}
          cy={h - ((v - mn) / rng) * h}
          r="2"
          fill={color}
        />
      ))}
    </svg>
  );
}

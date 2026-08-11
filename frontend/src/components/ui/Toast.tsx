import React, { useState, useCallback, useEffect, createContext, useContext } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastItem {
  id: string;
  message: string;
  type: ToastType;
  duration?: number;
}

interface ToastContextType {
  showToast: (message: string, type?: ToastType, duration?: number) => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

let globFn: ((message: string, type?: ToastType, duration?: number) => void) | null = null;

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [ts, setTs] = useState<ToastItem[]>([]);

  const showToast = useCallback((message: string, type: ToastType = 'info', duration = 3000) => {
    const id = Math.random().toString(36).slice(2, 11);
    setTs((prev) => [...prev, { id, message, type, duration }]);
    setTimeout(() => {
      setTs((prev) => prev.filter((t) => t.id !== id));
    }, duration + 300);
  }, []);

  useEffect(() => {
    globFn = showToast;
    return () => { globFn = null; };
  }, [showToast]);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed right-4 top-4 z-[100] flex flex-col gap-3">
        {ts.map((toast) => (
          <Toast key={toast.id} item={toast} onClose={() => setTs((prev) => prev.filter((t) => t.id !== toast.id))} />
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    return { showToast: globFn || (() => {}) };
  }
  return ctx;
}

export function showToast(message: string, type: ToastType = 'info', duration = 3000) {
  if (globFn) {
    globFn(message, type, duration);
  } else {
    console.warn('ToastProvider not mounted');
  }
}

function Toast({ item, onClose }: { item: ToastItem; onClose: () => void }) {
  useEffect(() => {
    const timer = setTimeout(onClose, item.duration || 3000);
    return () => clearTimeout(timer);
  }, [item.duration, onClose]);

  const map = {
    success: { icon: CheckCircle, cls: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-200' },
    error: { icon: AlertCircle, cls: 'border-red-500/30 bg-red-500/10 text-red-200' },
    warning: { icon: AlertTriangle, cls: 'border-amber-500/30 bg-amber-500/10 text-amber-200' },
    info: { icon: Info, cls: 'border-brand-500/30 bg-brand-500/10 text-teal-200' },
  };

  const { icon: Icon, cls } = map[item.type];

  return (
    <div className={`flex w-80 items-center gap-3 rounded-xl border px-4 py-3 shadow-2xl animate-slide-down ${cls}`}>
      <Icon className="h-5 w-5 shrink-0" />
      <p className="flex-1 text-sm font-medium">{item.message}</p>
      <button onClick={onClose} className="shrink-0 rounded-lg p-1 opacity-70 hover:opacity-100 hover:bg-white/5">
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

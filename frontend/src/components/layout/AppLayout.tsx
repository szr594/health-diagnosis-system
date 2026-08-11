import { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Menu, Heart } from 'lucide-react';
import Sidebar from './Sidebar';

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  return (
    <div className="flex min-h-screen">
      <Sidebar
        collapsed={collapsed}
        onToggle={() => setCollapsed((v) => !v)}
        mobileOpen={mobileOpen}
        onMobileClose={() => setMobileOpen(false)}
      />

      <main
        className={`flex-1 transition-all duration-300 ${
          collapsed ? 'lg:pl-20' : 'lg:pl-64'
        }`}
      >
        <header
          className="sticky top-0 z-20 flex items-center justify-between border-b px-4 py-3 backdrop-blur-xl lg:hidden"
          style={{
            borderColor: 'var(--border-white-006)',
            background: 'var(--bg-sidebar)',
          }}
        >
          <button
            onClick={() => setMobileOpen(true)}
            className="flex h-9 w-9 items-center justify-center rounded-lg transition-colors hover:bg-white/[0.06]"
            style={{ color: 'var(--text-primary)' }}
          >
            <Menu className="h-5 w-5" />
          </button>
          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-brand-400 to-brand-600">
              <Heart className="h-4 w-4 text-white" fill="currentColor" />
            </div>
            <span className="text-sm font-bold" style={{ color: 'var(--text-primary)' }}>智能问诊</span>
          </div>
          <div className="w-9" />
        </header>

        <div className="pointer-events-none fixed left-1/3 top-0 h-[600px] w-[600px] -translate-x-1/2 rounded-full bg-brand-500/5 blur-[120px]" />
        <div className="pointer-events-none fixed bottom-0 right-0 h-[400px] w-[400px] rounded-full bg-accent-500/5 blur-[100px]" />

        <div className="relative z-10 min-h-screen p-4 sm:p-6 lg:p-8">
          <div className="mx-auto max-w-6xl">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
}

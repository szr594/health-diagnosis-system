import { useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Stethoscope, BookOpen, History, LogOut, Heart, User,
  ChevronLeft, ChevronRight, Sun, Moon, X,
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useTheme } from '../../contexts/ThemeContext';
import ConfirmDialog from '../ui/ConfirmDialog';

const navItems = [
  { to: '/dashboard',    icon: LayoutDashboard, label: '工作台' },
  { to: '/consultation', icon: Stethoscope,     label: 'AI 问诊' },
  { to: '/history',      icon: History,         label: '问诊记录' },
  { to: '/knowledge',    icon: BookOpen,        label: '知识库' },
  { to: '/profile',     icon: User,            label: '个人中心' },
];

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
  mobileOpen: boolean;
  onMobileClose: () => void;
}

export default function Sidebar({ collapsed, onToggle, mobileOpen, onMobileClose }: SidebarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [logoutOpen, setLogoutOpen] = useState(false);

  const isActive = (path: string) =>
    location.pathname === path || location.pathname.startsWith(path + '/');

  function handleLogout() {
    logout();
    navigate('/login');
  }

  function handleNavClick() {
    if (mobileOpen) onMobileClose();
  }

  return (
    <>
      {mobileOpen && (
        <div
          className="fixed inset-0 z-30 bg-black/50 backdrop-blur-sm lg:hidden animate-fade-in"
          onClick={onMobileClose}
        />
      )}

      <aside
        className={`fixed left-0 top-0 z-40 flex h-screen flex-col border-r backdrop-blur-xl transition-all duration-300 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0 ${
          collapsed ? 'w-20' : 'w-64'
        }`}
        style={{
          borderColor: 'var(--border-white-006)',
          background: 'var(--bg-sidebar)',
        }}
      >
        <div className={`flex items-center gap-3 py-6 ${collapsed ? 'justify-center px-2' : 'px-6'}`}>
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-glow">
            <Heart className="h-5 w-5 text-white" fill="currentColor" />
          </div>
          {!collapsed && (
            <div className="animate-fade-in overflow-hidden whitespace-nowrap">
              <h1 className="text-base font-bold tracking-tight" style={{ color: 'var(--text-primary)' }}>智能问诊</h1>
              <p className="text-xs" style={{ color: 'var(--text-faint)' }}>Health AI Platform</p>
            </div>
          )}
          <button
            onClick={onMobileClose}
            className="absolute right-3 top-5 flex h-8 w-8 items-center justify-center rounded-lg transition-colors hover:bg-white/[0.06] lg:hidden"
            style={{ color: 'var(--text-secondary)' }}
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <button
          onClick={onToggle}
          className="absolute -right-3 top-[88px] hidden h-6 w-6 items-center justify-center rounded-full border shadow-lg transition-all hover:text-white lg:flex"
          style={{
            borderColor: 'var(--border-white-010)',
            background: 'var(--bg-toggle-btn)',
            color: 'var(--text-secondary)',
          }}
        >
          {collapsed ? <ChevronRight className="h-3.5 w-3.5" /> : <ChevronLeft className="h-3.5 w-3.5" />}
        </button>

        <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-2">
          {navItems.map(({ to, icon: Icon, label }) => {
            const active = isActive(to);
            return (
              <NavLink
                key={to}
                to={to}
                onClick={handleNavClick}
                title={collapsed ? label : undefined}
                className={`group flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200 ${
                  collapsed ? 'justify-center' : ''
                } ${
                  active
                    ? 'bg-brand-500/18 text-brand-200 shadow-[0_0_12px_rgba(20,184,166,0.12)] border border-brand-500/25'
                    : 'border border-transparent'
                } ${!active ? 'hover:bg-white/[0.06]' : ''}`}
                style={!active ? { color: 'var(--text-muted)' } : undefined}
              >
                <Icon className={`h-5 w-5 shrink-0 transition-transform duration-200 ${active ? 'scale-110' : 'group-hover:scale-105'}`} />
                {!collapsed && <span className="animate-fade-in overflow-hidden whitespace-nowrap" style={!active ? { color: 'var(--text-secondary)' } : undefined}>{label}</span>}
              </NavLink>
            );
          })}
        </nav>

        <div className="px-3 pb-2">
          <button
            onClick={toggleTheme}
            className={`flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-sm font-medium transition-all duration-200 ${
              collapsed ? 'justify-center' : ''
            }`}
            style={{ color: 'var(--text-secondary)' }}
          >
            {theme === 'dark' ? (
              <>
                <Sun className="h-5 w-5 shrink-0" />
                {!collapsed && <span className="animate-fade-in">浅色模式</span>}
              </>
            ) : (
              <>
                <Moon className="h-5 w-5 shrink-0" />
                {!collapsed && <span className="animate-fade-in">深色模式</span>}
              </>
            )}
          </button>
        </div>

        <div className="border-t p-4" style={{ borderColor: 'var(--border-white-006)' }}>
          <div className="relative">
            <button
              onClick={() => setUserMenuOpen((v) => !v)}
              className={`flex w-full items-center gap-3 rounded-xl p-3 transition-all duration-200 ${
                collapsed ? 'justify-center' : ''
              } ${isActive('/profile') ? 'bg-brand-500/15 border border-brand-500/20' : 'hover:bg-white/[0.06] border border-transparent'}`}
            >
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-brand-400 to-brand-600 text-sm font-bold text-white">
                {user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U'}
              </div>
              {!collapsed && (
                <div className="min-w-0 flex-1 text-left animate-fade-in">
                  <p className="text-sm font-medium truncate" style={{ color: 'var(--text-primary)' }}>
                    {user?.nickname || user?.username || '用户'}
                  </p>
                  <p className="text-xs truncate" style={{ color: 'var(--text-faint)' }}>
                    {user?.role === 2 ? '管理员' : user?.role === 1 ? '医生' : '患者'}
                  </p>
                </div>
              )}
            </button>

            {userMenuOpen && (
              <>
                <div
                  className="fixed inset-0 z-30"
                  onClick={() => setUserMenuOpen(false)}
                />
                <div
                  className={`absolute bottom-full z-40 mb-2 w-48 overflow-hidden rounded-xl border p-1.5 shadow-2xl backdrop-blur-xl animate-scale-in ${collapsed ? 'left-0' : 'right-0'}`}
                  style={{
                    borderColor: 'var(--border-white-008)',
                    background: 'var(--bg-dropdown)',
                  }}
                >
                  <button
                    onClick={() => { setUserMenuOpen(false); navigate('/profile'); handleNavClick(); }}
                    className="flex w-full items-center gap-2 rounded-lg px-3 py-2.5 text-sm transition-colors hover:bg-white/[0.06]"
                    style={{ color: 'var(--text-secondary)' }}
                  >
                    <User className="h-4 w-4" /> 个人中心
                  </button>
                  <div className="my-1 border-t" style={{ borderColor: 'var(--border-white-006)' }} />
                  <button
                    onClick={() => { setUserMenuOpen(false); setLogoutOpen(true); }}
                    className="flex w-full items-center gap-2 rounded-lg px-3 py-2.5 text-sm text-red-300 transition-colors hover:bg-red-500/10"
                  >
                    <LogOut className="h-4 w-4" /> 退出登录
                  </button>
                </div>
              </>
            )}
          </div>
        </div>

        <ConfirmDialog
          isOpen={logoutOpen}
          onClose={() => setLogoutOpen(false)}
          onConfirm={handleLogout}
          title="退出登录"
          message="确定要退出当前账号吗？"
          confirmText="退出"
          type="warning"
        />
      </aside>
    </>
  );
}

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, Eye, EyeOff, ArrowRight } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../components/ui/Toast';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);
  const { login } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) {
      showToast('请输入用户名', 'warning');
      setShake(true);
      setTimeout(() => setShake(false), 400);
      return;
    }
    if (!password.trim()) {
      showToast('请输入密码', 'warning');
      setShake(true);
      setTimeout(() => setShake(false), 400);
      return;
    }
    setLoading(true);
    try {
      await login({ username: username.trim(), password });
      showToast('登录成功', 'success');
      navigate('/dashboard');
    } catch (err: any) {
      showToast(err.response?.data?.message || '登录失败，请重试', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen">
      <div className="pointer-events-none fixed left-1/2 top-0 h-[600px] w-[600px] -translate-x-1/2 rounded-full bg-brand-500/8 blur-[120px]" />

      <div className="relative z-10 flex w-full flex-col items-center justify-center p-4 lg:w-1/2 lg:p-12">
        <div className="mb-6 w-full max-w-md overflow-hidden rounded-2xl border border-white/[0.08] lg:hidden">
          <img
            src="/hero-banner.jpg"
            alt="大健康智能问诊平台"
            className="h-32 w-full object-cover object-center"
          />
          <div className="bg-brand-950/80 px-4 py-2">
            <h2 className="text-base font-bold text-on-image">大健康智能问诊平台</h2>
            <p className="text-xs text-on-image-faint">症状自查、健康咨询、慢病管理</p>
          </div>
        </div>

        <div className={`glass-panel-strong w-full max-w-md p-8 animate-slide-up ${shake ? 'animate-shake' : ''}`}>
          <div className="mb-8 text-center">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-glow">
              <Heart className="h-7 w-7 text-white" fill="currentColor" />
            </div>
            <h2 className="text-2xl font-bold text-white">欢迎回来</h2>
            <p className="mt-1 text-sm text-white/40">大健康智能问诊平台</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="input-label">用户名</label>
              <input
                type="text"
                className="input-field"
                placeholder="请输入用户名"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                disabled={loading}
              />
            </div>

            <div>
              <label className="input-label">密码</label>
              <div className="relative">
                <input
                  type={showPwd ? 'text' : 'password'}
                  className="input-field pr-10"
                  placeholder="请输入密码"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowPwd(!showPwd)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-white/30 hover:text-white/60"
                  disabled={loading}
                >
                  {showPwd ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full"
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                  登录中...
                </span>
              ) : (
                <>登 录</>
              )}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button
              onClick={() => navigate('/register')}
              className="inline-flex items-center gap-1 rounded-xl border border-white/[0.08] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-white/70 transition-all hover:bg-white/[0.07] hover:text-white hover:shadow-lg"
            >
              还没有账号？立即注册 <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>

          <p className="mt-4 text-center text-xs text-white/25">
            登录即表示您同意平台的用户协议与隐私政策
          </p>
        </div>
      </div>

      <div className="relative hidden w-1/2 overflow-hidden lg:block">
        <img
          src="/hero-banner.jpg"
          alt="大健康智能问诊平台"
          className="absolute inset-0 h-full w-full object-cover object-center"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-brand-950/90 via-brand-950/50 to-brand-950/20" />
        <div className="absolute inset-0 bg-brand-950/20" />
        <div className="absolute bottom-10 left-10 max-w-sm">
          <h2 className="text-3xl font-bold text-on-image">大健康智能问诊平台</h2>
          <p className="mt-2 text-sm text-on-image-muted">症状自查、健康咨询、慢病管理、用药提醒</p>
        </div>
      </div>
    </div>
  );
}

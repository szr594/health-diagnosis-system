import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Heart, Eye, EyeOff, ArrowLeft, Check } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../components/ui/Toast';

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', password: '', confirmPwd: '', nickname: '', phone: '' });
  const [showPwd, setShowPwd] = useState(false);
  const [agree, setAgree] = useState(false);
  const [loading, setLoading] = useState(false);
  const [shake, setShake] = useState(false);
  const { register } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const update = (k: string, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.username.trim() || form.username.length < 3) {
      showToast('用户名至少3个字符', 'warning');
      setShake(true); setTimeout(() => setShake(false), 400);
      return;
    }
    if (form.password.length < 6) {
      showToast('密码至少6个字符', 'warning');
      setShake(true); setTimeout(() => setShake(false), 400);
      return;
    }
    if (form.password !== form.confirmPwd) {
      showToast('两次密码输入不一致', 'warning');
      setShake(true); setTimeout(() => setShake(false), 400);
      return;
    }
    if (!agree) {
      showToast('请阅读并同意用户协议与隐私政策', 'warning');
      setShake(true); setTimeout(() => setShake(false), 400);
      return;
    }
    setLoading(true);
    try {
      await register({
        username: form.username.trim(),
        password: form.password,
        nickname: form.nickname.trim() || undefined,
        phone: form.phone.trim() || undefined,
      });
      showToast('注册成功', 'success');
      navigate('/dashboard');
    } catch (err: any) {
      showToast(err.response?.data?.message || '注册失败，请重试', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen">
      <div className="pointer-events-none fixed left-1/2 top-0 h-[600px] w-[600px] -translate-x-1/2 rounded-full bg-brand-500/8 blur-[120px]" />

      <div className="relative hidden w-1/2 overflow-hidden lg:block">
        <img
          src="/hero-banner.jpg"
          alt="大健康智能问诊平台"
          className="absolute inset-0 h-full w-full object-cover object-center"
        />
        <div className="absolute inset-0 bg-gradient-to-l from-brand-950/90 via-brand-950/50 to-brand-950/20" />
        <div className="absolute inset-0 bg-brand-950/20" />
        <div className="absolute bottom-10 right-10 max-w-sm text-right">
          <h2 className="text-3xl font-bold text-on-image">加入大健康智能问诊平台</h2>
          <p className="mt-2 text-sm text-on-image-muted">开启您的 AI 健康助手之旅</p>
        </div>
      </div>

      <div className="relative z-10 flex w-full flex-col items-center justify-center p-4 lg:w-1/2 lg:p-12">
        <div className="mb-6 w-full max-w-md overflow-hidden rounded-2xl border border-white/[0.08] lg:hidden">
          <img
            src="/hero-banner.jpg"
            alt="大健康智能问诊平台"
            className="h-32 w-full object-cover object-center"
          />
          <div className="bg-brand-950/80 px-4 py-2">
            <h2 className="text-base font-bold text-on-image">加入大健康智能问诊平台</h2>
            <p className="text-xs text-on-image-faint">开启 AI 健康助手之旅</p>
          </div>
        </div>

        <div className={`glass-panel-strong w-full max-w-md p-8 animate-slide-up ${shake ? 'animate-shake' : ''}`}>
          <Link to="/login" className="mb-6 inline-flex items-center gap-1 text-sm text-white/40 hover:text-white/70 transition-colors">
            <ArrowLeft className="h-4 w-4" /> 返回登录
          </Link>

          <div className="mb-8 text-center">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-glow">
              <Heart className="h-7 w-7 text-white" fill="currentColor" />
            </div>
            <h2 className="text-2xl font-bold text-white">创建账号</h2>
            <p className="mt-1 text-sm text-white/40">加入大健康智能问诊平台</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="input-label">用户名 <span className="text-red-400">*</span></label>
              <input type="text" className="input-field" placeholder="3-20个字符" value={form.username}
                onChange={(e) => update('username', e.target.value)} autoComplete="username" disabled={loading} />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="input-label">密码 <span className="text-red-400">*</span></label>
                <div className="relative">
                  <input type={showPwd ? 'text' : 'password'} className="input-field pr-10" placeholder="至少6位"
                    value={form.password} onChange={(e) => update('password', e.target.value)} autoComplete="new-password" disabled={loading} />
                  <button type="button" onClick={() => setShowPwd(!showPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 text-white/30 hover:text-white/60" disabled={loading}>
                    {showPwd ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>
              <div>
                <label className="input-label">确认密码 <span className="text-red-400">*</span></label>
                <input type="password" className="input-field" placeholder="再次输入密码"
                  value={form.confirmPwd} onChange={(e) => update('confirmPwd', e.target.value)} autoComplete="new-password" disabled={loading} />
              </div>
            </div>

            <div>
              <label className="input-label">昵称</label>
              <input type="text" className="input-field" placeholder="选填" value={form.nickname}
                onChange={(e) => update('nickname', e.target.value)} disabled={loading} />
            </div>

            <div>
              <label className="input-label">手机号</label>
              <input type="tel" className="input-field" placeholder="选填" value={form.phone}
                onChange={(e) => update('phone', e.target.value)} autoComplete="tel" disabled={loading} />
            </div>

            <label className="flex cursor-pointer items-start gap-2 rounded-lg py-1">
              <button
                type="button"
                onClick={() => setAgree((v) => !v)}
                className={`mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded border transition-all ${
                  agree ? 'border-brand-400 bg-brand-500 text-white' : 'border-white/20 bg-white/5'
                }`}
              >
                {agree && <Check className="h-3 w-3" />}
              </button>
              <span className="text-xs leading-relaxed text-white/50">
                我已阅读并同意
                <button type="button" className="mx-0.5 text-brand-400 hover:text-brand-300">用户协议</button>
                和
                <button type="button" className="mx-0.5 text-brand-400 hover:text-brand-300">隐私政策</button>
              </span>
            </label>

            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                  注册中...
                </span>
              ) : (
                <>注 册</>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

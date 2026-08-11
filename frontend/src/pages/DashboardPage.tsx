import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Stethoscope, BookOpen, History, TrendingUp, ArrowRight, Sparkles, Clock, Activity, FileText, LayoutDashboard } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { consultationApi } from '../services/api';
import { GlassCard, StatCard, MiniChart } from '../components/ui/GlassCard';
import PageHeader from '../components/ui/PageHeader';
import MedicalDisclaimer from '../components/ui/MedicalDisclaimer';
import RiskBadge from '../components/ui/RiskBadge';
import EmptyState from '../components/ui/EmptyState';
import type { HotQuestionVO, ConsultationVO } from '../types';

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [hotQuestions, setHotQuestions] = useState<HotQuestionVO[]>([]);
  const [recentRecords, setRecentRecords] = useState<ConsultationVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      consultationApi.hotQuestions().then((r) => setHotQuestions(r.data.data || [])).catch(() => {}),
      consultationApi.list(1, 5).then((r) => setRecentRecords(r.data.data?.records || [])).catch(() => {}),
    ]).finally(() => setLoading(false));
  }, []);

  const displayName = user?.nickname || user?.username || '用户';

  return (
    <div className="space-y-8 animate-fade-in">
      <PageHeader title="工作台" subtitle={`欢迎回来，${displayName}`} icon={LayoutDashboard} />

      <div className="group relative overflow-hidden rounded-2xl border border-white/[0.08] shadow-2xl shadow-brand-500/10">
        <div className="relative aspect-[21/9] w-full overflow-hidden bg-brand-900/30 md:aspect-[21/8]">
          <img
            src="/hero-banner.jpg"
            alt="大健康智能问诊平台"
            className="h-full w-full object-cover object-center transition-transform duration-700 group-hover:scale-105"
          />
          <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-brand-950/85 via-brand-950/50 to-transparent" />
          <div className="pointer-events-none absolute inset-0 bg-brand-950/25" />
        </div>
        <div className="absolute bottom-0 left-0 right-0 flex items-end justify-between p-5 md:p-7">
          <div className="hidden md:block">
            <p className="text-sm font-medium text-on-image-muted">你好，{displayName}</p>
            <p className="text-xs text-on-image-faint">症状自查 · 健康咨询 · 慢病管理 · 用药提醒</p>
          </div>
          <button
            onClick={() => navigate('/consultation')}
            className="btn-primary px-6 py-3 text-sm md:px-8 md:text-base transition-all duration-200 hover:shadow-[0_0_30px_rgba(20,184,166,0.35)]"
          >
            <Sparkles className="h-4 w-4 md:h-5 md:w-5" />
            开始 AI 问诊
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <StatCard icon={Stethoscope} label="今日问诊" value={recentRecords.length} color="brand">
          <MiniChart data={[1, 3, 2, 5, 4, 6, recentRecords.length || 2]} />
        </StatCard>
        <StatCard icon={BookOpen} label="知识库条目" value="6" color="accent">
          <MiniChart data={[4, 5, 5, 6, 6, 6, 6]} color="#2dd4bf" />
        </StatCard>
        <StatCard icon={History} label="累计记录" value={recentRecords.length} color="warning">
          <MiniChart data={[0, 1, 1, 2, 3, 4, recentRecords.length || 1]} color="#fbbf24" />
        </StatCard>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <div className="lg:col-span-3">
          <GlassCard>
            <div className="mb-5 flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-brand-400" />
              <h3 className="text-lg font-semibold text-white">热门问诊</h3>
            </div>
            {loading ? (
              <div className="space-y-2">
                {Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="h-12 animate-pulse rounded-xl bg-white/[0.05]" />
                ))}
              </div>
            ) : hotQuestions.length === 0 ? (
              <EmptyState title="暂无热门问诊" description="还没有足够的问诊数据" icon={Activity} />
            ) : (
              <div className="space-y-2">
                {hotQuestions.map((q, i) => (
                  <button
                    key={i}
                    onClick={() => navigate('/consultation', { state: { symptom: q.symptom } })}
                    className="flex w-full items-center justify-between rounded-xl px-4 py-3 text-left transition-all duration-200 hover:bg-white/[0.06] hover:translate-x-1"
                  >
                    <div className="flex items-center gap-3">
                      <span className={`flex h-6 w-6 items-center justify-center rounded-lg text-xs font-bold ${
                        i === 0 ? 'bg-amber-500/20 text-amber-400' :
                        i === 1 ? 'bg-gray-400/20 text-gray-300' :
                        i === 2 ? 'bg-amber-700/20 text-amber-600' :
                        'bg-white/5 text-white/30'
                      }`}>{i + 1}</span>
                      <span className="text-sm text-white/85">{q.symptom}</span>
                    </div>
                    <div className="flex items-center gap-2 text-xs text-white/35 transition-colors group-hover:text-white/60">
                      <span>{q.count} 次</span>
                      <ArrowRight className="h-3.5 w-3.5" />
                    </div>
                  </button>
                ))}
              </div>
            )}
          </GlassCard>
        </div>

        <div className="lg:col-span-2">
          <GlassCard>
            <div className="mb-5 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Clock className="h-5 w-5 text-brand-400" />
                <h3 className="text-lg font-semibold text-white">最近问诊</h3>
              </div>
              <button
                onClick={() => navigate('/history')}
                className="group flex items-center gap-1 text-xs text-brand-400 hover:text-brand-300 transition-colors"
              >
                查看全部 <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5" />
              </button>
            </div>
            {loading ? (
              <div className="space-y-3">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="h-16 animate-pulse rounded-xl bg-white/[0.05]" />
                ))}
              </div>
            ) : recentRecords.length === 0 ? (
              <EmptyState
                title="暂无问诊记录"
                description="开始您的第一次 AI 问诊"
                icon={FileText}
                action={
                  <button onClick={() => navigate('/consultation')} className="btn-primary py-2 text-xs">
                    开始问诊
                  </button>
                }
              />
            ) : (
              <div className="space-y-3">
                {recentRecords.map((r) => (
                  <button
                    key={r.id}
                    onClick={() => navigate(`/history/${r.id}`)}
                    className="flex w-full items-center justify-between rounded-xl px-4 py-3 text-left transition-all duration-200 hover:bg-white/[0.06] hover:translate-x-1"
                  >
                    <div className="min-w-0 flex-1 text-left">
                      <p className="text-sm text-white/85 truncate">{r.symptomDescription}</p>
                      <p className="mt-0.5 text-xs text-white/35">
                        {r.createTime ? new Date(r.createTime).toLocaleDateString('zh-CN') : ''}
                      </p>
                    </div>
                    <RiskBadge level={r.riskLevel} />
                  </button>
                ))}
              </div>
            )}
          </GlassCard>
        </div>
      </div>

      <MedicalDisclaimer className="mt-4" />
    </div>
  );
}

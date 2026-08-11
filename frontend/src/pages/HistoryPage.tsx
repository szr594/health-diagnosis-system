import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { History, Trash2, Clock, Eye, Filter, X, FileText, Stethoscope } from 'lucide-react';
import { consultationApi } from '../services/api';
import { useToast } from '../components/ui/Toast';
import { GlassCard } from '../components/ui/GlassCard';
import PageHeader from '../components/ui/PageHeader';
import MedicalDisclaimer from '../components/ui/MedicalDisclaimer';
import RiskBadge from '../components/ui/RiskBadge';
import EmptyState from '../components/ui/EmptyState';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import Skeleton from '../components/ui/Skeleton';
import type { ConsultationVO, PageResult } from '../types';

const riskOptions = [
  { value: '', label: '全部风险' },
  { value: 'low', label: '低风险' },
  { value: 'mid', label: '中风险' },
  { value: 'high', label: '高风险' },
];

export default function HistoryPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [records, setRecords] = useState<ConsultationVO[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<ConsultationVO | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ConsultationVO | null>(null);
  const [riskFilter, setRiskFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');

  const fetchData = async (pn: number) => {
    setLoading(true);
    try {
      const r = await consultationApi.list(pn, 10);
      const d: PageResult<ConsultationVO> = r.data.data;
      setRecords(d.records || []);
      setTotal(d.total || 0);
    } catch {
      showToast('加载问诊记录失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(pageNum); }, [pageNum]);

  const handleDelete = async (rid: number) => {
    try {
      await consultationApi.delete(rid);
      showToast('删除成功', 'success');
      fetchData(pageNum);
    } catch {
      showToast('删除失败', 'error');
    }
  };

  const filteredRecords = useMemo(() => {
    return records.filter((r) => {
      if (riskFilter && r.riskLevel !== riskFilter) return false;
      if (dateFilter && r.createTime) {
        const d = new Date(r.createTime).toISOString().slice(0, 10);
        if (d !== dateFilter) return false;
      }
      return true;
    });
  }, [records, riskFilter, dateFilter]);

  function statusBadge(status: number) {
    if (status === 0) return <span className="badge badge-info">处理中</span>;
    if (status === 2) return <span className="badge badge-danger">失败</span>;
    return <span className="badge badge-success">已完成</span>;
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <PageHeader
        title="问诊记录"
        subtitle={`共 ${total} 条记录`}
        icon={History}
        breadcrumbs={[{ label: '工作台', to: '/dashboard' }, { label: '问诊记录' }]}
      />

      <div className="flex flex-col gap-3 rounded-xl border border-white/[0.08] bg-white/[0.03] p-3 sm:flex-row sm:items-center">
        <div className="flex items-center gap-2 text-sm text-white/50">
          <Filter className="h-4 w-4" /> 筛选
        </div>
        <div className="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center">
          <select
            value={riskFilter}
            onChange={(e) => setRiskFilter(e.target.value)}
            className="input-field w-full py-2.5 sm:w-40"
          >
            {riskOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <input
            type="date"
            value={dateFilter}
            onChange={(e) => setDateFilter(e.target.value)}
            className="input-field w-full py-2.5 sm:w-44"
          />
          {(riskFilter || dateFilter) && (
            <button
              onClick={() => { setRiskFilter(''); setDateFilter(''); }}
              className="inline-flex items-center gap-1 self-start rounded-lg px-2 py-1 text-xs text-white/50 hover:bg-white/[0.05] hover:text-white"
            >
              <X className="h-3.5 w-3.5" /> 清除筛选
            </button>
          )}
        </div>
      </div>

      <div className="space-y-3">
        {loading ? (
          <>
            <Skeleton className="h-28" />
            <Skeleton className="h-28" />
            <Skeleton className="h-28" />
          </>
        ) : filteredRecords.length === 0 ? (
          <GlassCard>
            <EmptyState
              title="暂无问诊记录"
              description={records.length === 0 ? "开始您的第一次 AI 问诊" : "当前筛选条件下没有记录"}
              icon={FileText}
              action={
                records.length === 0 ? (
                  <button onClick={() => navigate('/consultation')} className="btn-primary py-2 text-xs">
                    <Stethoscope className="h-3.5 w-3.5" /> 开始问诊
                  </button>
                ) : (
                  <button onClick={() => { setRiskFilter(''); setDateFilter(''); }} className="btn-secondary py-2 text-xs">
                    清除筛选
                  </button>
                )
              }
            />
          </GlassCard>
        ) : (
          filteredRecords.map((r) => (
            <GlassCard key={r.id} hover className="group">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0 flex-1">
                  <div className="mb-2 flex flex-wrap items-center gap-2">
                    <RiskBadge level={r.riskLevel} />
                    {statusBadge(r.status)}
                    {r.suggestedDepartment && <span className="badge badge-info">{r.suggestedDepartment}</span>}
                  </div>
                  <p className="text-sm text-white/85 line-clamp-2">{r.symptomDescription}</p>
                  <div className="mt-2 flex items-center gap-3 text-xs text-white/40">
                    <span className="flex items-center gap-1"><Clock className="h-3.5 w-3.5" /> {r.createTime ? new Date(r.createTime).toLocaleString('zh-CN') : ''}</span>
                    {r.possibleDiseases && <span>疑似：{r.possibleDiseases}</span>}
                  </div>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-2">
                  <button
                    onClick={() => setSelected(r)}
                    className="inline-flex items-center gap-1 rounded-lg bg-white/[0.05] px-3 py-1.5 text-xs text-white/70 transition-all hover:bg-brand-500/15 hover:text-brand-200"
                  >
                    <Eye className="h-3.5 w-3.5" /> 查看详情
                  </button>
                  <button
                    onClick={() => setDeleteTarget(r)}
                    className="inline-flex items-center gap-1 rounded-lg px-2 py-1.5 text-xs text-white/35 transition-all hover:bg-red-500/10 hover:text-red-400"
                  >
                    <Trash2 className="h-3.5 w-3.5" /> 删除
                  </button>
                </div>
              </div>
            </GlassCard>
          ))
        )}
      </div>

      {total > 10 && (
        <div className="flex items-center justify-center gap-2">
          <button onClick={() => setPageNum((p) => Math.max(1, p - 1))} disabled={pageNum === 1}
            className="btn-ghost disabled:opacity-30">上一页</button>
          <span className="text-sm text-white/40">第 {pageNum} / {Math.ceil(total / 10)} 页</span>
          <button onClick={() => setPageNum((p) => p + 1)} disabled={pageNum >= Math.ceil(total / 10)}
            className="btn-ghost disabled:opacity-30">下一页</button>
        </div>
      )}

      <MedicalDisclaimer />

      <Modal
        isOpen={!!selected}
        onClose={() => setSelected(null)}
        title="问诊详情"
        width="lg"
        footer={
          <button onClick={() => setSelected(null)} className="btn-secondary">关闭</button>
        }
      >
        {selected && (
          <div className="space-y-5">
            <div className="flex flex-wrap items-center gap-2">
              <RiskBadge level={selected.riskLevel} />
              {selected.suggestedDepartment && <span className="badge badge-info">{selected.suggestedDepartment}</span>}
            </div>
            <div>
              <h4 className="mb-1 text-sm font-semibold text-white/70">症状描述</h4>
              <p className="text-sm leading-relaxed text-white/85">{selected.symptomDescription}</p>
              {selected.symptomDuration && <p className="mt-1 text-xs text-white/40">持续时间：{selected.symptomDuration}</p>}
            </div>
            {selected.possibleDiseases && (
              <div>
                <h4 className="mb-1 text-sm font-semibold text-white/70">疑似疾病</h4>
                <p className="text-sm text-white/85">{selected.possibleDiseases}</p>
              </div>
            )}
            {selected.aiAdvice && (
              <div>
                <h4 className="mb-1 text-sm font-semibold text-white/70">AI 建议</h4>
                <p className="whitespace-pre-wrap text-sm leading-relaxed text-white/80">{selected.aiAdvice}</p>
              </div>
            )}
            <div className="text-xs text-white/30">
              问诊时间：{selected.createTime ? new Date(selected.createTime).toLocaleString('zh-CN') : '-'}
            </div>
            <MedicalDisclaimer />
          </div>
        )}
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => deleteTarget && handleDelete(deleteTarget.id)}
        title="删除问诊记录"
        message="确定要删除这条问诊记录吗？删除后无法恢复。"
        confirmText="删除"
        type="danger"
      />
    </div>
  );
}

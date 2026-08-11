import { useEffect, useMemo, useState } from 'react';
import { BookOpen, Search, ChevronRight, Clock, Tag, X, FileText } from 'lucide-react';
import { knowledgeApi } from '../services/api';
import { useToast } from '../components/ui/Toast';
import { GlassCard } from '../components/ui/GlassCard';
import PageHeader from '../components/ui/PageHeader';
import MedicalDisclaimer from '../components/ui/MedicalDisclaimer';
import EmptyState from '../components/ui/EmptyState';
import Modal from '../components/ui/Modal';
import Skeleton from '../components/ui/Skeleton';
import type { HealthKnowledge, PageResult } from '../types';

const categories = ['全部', '心血管', '呼吸', '内分泌', '消化', '精神心理'];
const categoryColor: Record<string, string> = {
  '心血管': 'badge-danger',
  '呼吸': 'badge-info',
  '内分泌': 'badge-warning',
  '消化': 'badge-success',
  '精神心理': 'badge-info',
};

export default function KnowledgePage() {
  const { showToast } = useToast();
  const [items, setItems] = useState<HealthKnowledge[]>([]);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [pageNum, setPageNum] = useState(1);
  const [selected, setSelected] = useState<HealthKnowledge | null>(null);
  const [category, setCategory] = useState('全部');
  const [loading, setLoading] = useState(true);

  const fetchData = async (kw?: string, pn = 1) => {
    setLoading(true);
    try {
      const r = await knowledgeApi.list(kw || undefined, pn, 10);
      const d: PageResult<HealthKnowledge> = r.data.data;
      setItems(d.records || []);
      setTotal(d.total || 0);
    } catch {
      showToast('加载知识库失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(keyword, pageNum); }, [pageNum]);

  const handleSearch = () => {
    setKeyword(searchInput);
    setPageNum(1);
    fetchData(searchInput, 1);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const filteredItems = useMemo(() => {
    if (category === '全部') return items;
    return items.filter((i) => i.category === category);
  }, [items, category]);

  return (
    <div className="space-y-6 animate-fade-in">
      <PageHeader
        title="健康知识库"
        subtitle={`共 ${total} 篇知识文档`}
        icon={BookOpen}
        breadcrumbs={[{ label: '工作台', to: '/dashboard' }, { label: '健康知识库' }]}
      />

      <div className="flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-white/30" />
          <input
            type="text"
            className="input-field pl-11"
            placeholder="搜索健康知识，按 Enter 快速搜索..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={handleKeyDown}
          />
        </div>
        <button onClick={handleSearch} className="btn-primary">搜索</button>
      </div>

      <div className="flex flex-wrap gap-2">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setCategory(cat)}
            className={`rounded-full px-4 py-1.5 text-sm font-medium transition-all duration-200 ${
              category === cat
                ? 'bg-brand-500/20 text-brand-200 border border-brand-500/30'
                : 'border border-white/[0.08] bg-white/[0.03] text-white/55 hover:bg-white/[0.06] hover:text-white'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      <div className="space-y-3">
        {loading ? (
          <>
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </>
        ) : filteredItems.length === 0 ? (
          <GlassCard>
            <EmptyState
              title="暂无知识文档"
              description={items.length === 0 ? "知识库正在建设中" : "当前分类/搜索条件下没有结果"}
              icon={FileText}
              action={
                items.length === 0 ? undefined : (
                  <button onClick={() => { setSearchInput(''); setKeyword(''); setCategory('全部'); fetchData('', 1); }} className="btn-secondary py-2 text-xs">
                    <X className="h-3.5 w-3.5" /> 清除筛选
                  </button>
                )
              }
            />
          </GlassCard>
        ) : (
          filteredItems.map((item) => (
            <GlassCard
              key={item.id}
              hover
              onClick={() => setSelected(item)}
              className="group transition-all duration-200"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0 flex-1">
                  <div className="mb-2 flex items-center gap-2">
                    {item.category && (
                      <span className={`badge ${categoryColor[item.category] || 'badge-info'}`}>
                        <Tag className="h-3 w-3" /> {item.category}
                      </span>
                    )}
                  </div>
                  <h3 className="text-base font-semibold text-white mb-1.5 group-hover:text-brand-200 transition-colors">{item.title}</h3>
                  <p className="text-sm text-white/45 line-clamp-2">{item.content}</p>
                  <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-white/30">
                    {item.createTime && (
                      <span className="flex items-center gap-1">
                        <Clock className="h-3.5 w-3.5" />
                        {new Date(item.createTime).toLocaleDateString('zh-CN')}
                      </span>
                    )}
                    {item.source && <span>来源：{item.source}</span>}
                  </div>
                </div>
                <ChevronRight className="h-5 w-5 shrink-0 text-white/20 transition-all group-hover:text-brand-300 group-hover:translate-x-1" />
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
        title={selected?.title || '知识详情'}
        width="lg"
        footer={
          <button onClick={() => setSelected(null)} className="btn-secondary">关闭</button>
        }
      >
        {selected && (
          <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-2">
              {selected.category && (
                <span className={`badge ${categoryColor[selected.category] || 'badge-info'}`}>
                  <Tag className="h-3 w-3" /> {selected.category}
                </span>
              )}
              {selected.source && <span className="text-xs text-white/35">来源：{selected.source}</span>}
              {selected.createTime && (
                <span className="flex items-center gap-1 text-xs text-white/35">
                  <Clock className="h-3.5 w-3.5" />
                  {new Date(selected.createTime).toLocaleDateString('zh-CN')}
                </span>
              )}
            </div>
            <p className="whitespace-pre-wrap text-sm leading-relaxed text-white/80">{selected.content}</p>
            <MedicalDisclaimer />
          </div>
        )}
      </Modal>
    </div>
  );
}

import { useState, useRef, useEffect, useCallback } from 'react';
import { useLocation } from 'react-router-dom';
import { Send, User, Bot, Stethoscope, Loader2, Clock, Activity, MessageSquare, Plus } from 'lucide-react';
import { consultationApi } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../components/ui/Toast';
import PageHeader from '../components/ui/PageHeader';
import MedicalDisclaimer from '../components/ui/MedicalDisclaimer';
import RiskBadge from '../components/ui/RiskBadge';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import type { ConsultationVO } from '../types';

interface Message {
  role: 'user' | 'ai';
  content: string;
  result?: ConsultationVO;
  loading?: boolean;
}

const quickSymptoms = ['头痛头晕三天', '胸闷气短', '发热咳嗽', '腹痛腹泻'];

export default function ConsultationPage() {
  const location = useLocation();
  const { user } = useAuth();
  const { showToast } = useToast();

  const [sessionKey, setSessionKey] = useState<string>('');
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [duration, setDuration] = useState('');
  const [loading, setLoading] = useState(false);
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => { messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  useEffect(() => {
    const state = location.state as any;
    if (state?.symptom) {
      setInput(state.symptom);
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const handleSend = useCallback(async () => {
    if (!input.trim()) {
      showToast('请先输入症状描述', 'warning');
      return;
    }
    if (loading) return;

    const symptom = input.trim();
    const userMsg: Message = { role: 'user', content: symptom };
    const aiMsg: Message = { role: 'ai', content: '', loading: true };
    setMessages((m) => [...m, userMsg, aiMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await consultationApi.preDiagnosis({
        sessionId: sessionKey || undefined,
        symptomDescription: symptom,
        symptomDuration: duration.trim() || undefined,
        age: user?.age || undefined,
        gender: user?.gender === 1 ? 'male' : user?.gender === 2 ? 'female' : 'unknown',
        medicalHistory: user?.medicalHistory || undefined,
        allergyHistory: user?.allergyHistory || undefined,
      });

      const data: ConsultationVO = res.data.data;

      if (!sessionKey && data.sessionKey) {
        setSessionKey(data.sessionKey);
        localStorage.setItem('consultation_session_key', data.sessionKey);
      }

      setMessages((m) => {
        const updated = [...m];
        updated[updated.length - 1] = {
          role: 'ai',
          content: data.aiAdvice || data.structuredAdvice || '已收到您的描述，请查看分析结果。',
          result: data,
        };
        return updated;
      });

      if (messages.length === 0) {
        showToast('AI 诊断完成', 'success');
      }
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'AI 服务暂时不可用，请稍后重试';
      setMessages((m) => {
        const updated = [...m];
        updated[updated.length - 1] = { role: 'ai', content: `抱歉，${errMsg}` };
        return updated;
      });
      showToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  }, [input, loading, sessionKey, duration, user, messages.length, showToast]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
  };

  function handleNewConversation() {
    setSessionKey('');
    setMessages([]);
    setInput('');
    setDuration('');
    setShowClearConfirm(false);
    localStorage.removeItem('consultation_session_key');
  }

  const hasStarted = messages.length > 0;

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col animate-fade-in">
      <div className="flex items-start justify-between">
        <PageHeader
          title="AI 智能问诊"
          subtitle="请详细描述您的症状，AI 助手将进行多轮追问和初步评估"
          icon={Stethoscope}
          breadcrumbs={[{ label: '工作台', to: '/dashboard' }, { label: 'AI 问诊' }]}
        />
        {hasStarted && (
          <button
            onClick={() => setShowClearConfirm(true)}
            className="mt-2 flex items-center gap-1.5 rounded-lg border border-white/[0.10] bg-white/[0.04] px-3 py-1.5 text-xs text-white/50 transition-all duration-200 hover:border-red-500/30 hover:bg-red-500/10 hover:text-red-400"
            disabled={loading}
          >
            <Plus className="h-3.5 w-3.5" />
            新对话
          </button>
        )}
      </div>

      <div className="flex-1 overflow-y-auto rounded-2xl glass-panel p-4 md:p-6 mb-4 space-y-5 scroll-area">
        {!hasStarted && (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="mb-6 h-44 w-80 overflow-hidden rounded-2xl border border-white/[0.08] shadow-glow">
              <img
                src="/hero-banner.jpg"
                alt="大健康智能问诊平台"
                className="h-full w-full object-cover object-center"
              />
            </div>
            <h3 className="text-xl font-semibold text-white">AI 预问诊助手</h3>
            <p className="mt-2 max-w-md text-sm text-white/45">
              请描述您的症状，包括不适部位、持续时间、伴随症状等。
              <br />AI 将综合您的个人信息进行初步评估，并进行必要的追问。
            </p>
            <div className="mt-6 flex flex-wrap justify-center gap-2">
              {quickSymptoms.map((q) => (
                <button
                  key={q}
                  onClick={() => setInput(q)}
                  className="rounded-xl border border-white/[0.10] bg-white/[0.04] px-4 py-2 text-sm text-white/55 transition-all duration-200 hover:border-brand-500/30 hover:bg-brand-500/10 hover:text-brand-200 hover:-translate-y-0.5"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        )}

        {hasStarted && (
          <div className="flex items-center justify-center">
            <span className="rounded-full border border-white/[0.08] bg-white/[0.03] px-4 py-1.5 text-xs text-white/35">
              共 {Math.floor(messages.length / 2)} 轮对话{messages.length > 6 ? '（多轮问诊中）' : ''}
              {sessionKey ? ` · ${sessionKey.slice(0, 8)}...` : ''}
            </span>
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : ''}`}>
            {msg.role === 'ai' && (
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-glow">
                <Bot className="h-5 w-5 text-white" />
              </div>
            )}
            <div className={`max-w-[85%] md:max-w-[75%] ${msg.role === 'user' ? 'order-first' : ''}`}>
              <div className={`rounded-2xl px-5 py-3.5 shadow-sm ${
                msg.role === 'user'
                  ? 'bg-gradient-to-br from-brand-500/20 to-brand-600/10 border border-brand-500/25'
                  : 'glass-panel'
              }`}>
                {msg.loading ? (
                  <div className="flex items-center gap-2 text-sm text-white/60">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    {i <= 2 ? 'AI 正在分析您的症状...' : 'AI 正在结合历史对话进行分析...'}
                  </div>
                ) : (
                  <>
                    <p className="text-sm leading-relaxed text-white/85 whitespace-pre-wrap">{msg.content}</p>
                    {msg.result && (
                      <div className="mt-4 space-y-4 border-t border-white/[0.08] pt-4">
                        <div className="flex flex-wrap items-center gap-2">
                          <RiskBadge level={msg.result.riskLevel} />
                          {msg.result.suggestedDepartment && (
                            <span className="badge badge-info">{msg.result.suggestedDepartment}</span>
                          )}
                        </div>
                        {msg.result.possibleDiseases && (
                          <div>
                            <p className="mb-1.5 flex items-center gap-1 text-xs font-medium text-white/50">
                              <Activity className="h-3.5 w-3.5" /> 疑似疾病
                            </p>
                            <p className="text-sm text-white/80">{msg.result.possibleDiseases}</p>
                          </div>
                        )}
                        {msg.result.aiAdvice && (
                          <div>
                            <p className="mb-1.5 flex items-center gap-1 text-xs font-medium text-white/50">
                              <MessageSquare className="h-3.5 w-3.5" /> 建议
                            </p>
                            <p className="text-sm leading-relaxed text-white/80">{msg.result.aiAdvice}</p>
                          </div>
                        )}
                        <MedicalDisclaimer className="mt-3" />
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
            {msg.role === 'user' && (
              <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-white/10">
                <User className="h-5 w-5 text-white/60" />
              </div>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <div className="shrink-0 glass-panel-strong p-4">
        {hasStarted && (
          <div className="mb-3 flex items-center gap-2">
            <Clock className="h-4 w-4 text-white/40" />
            <input
              type="text"
              className="input-field flex-1 py-2.5 text-sm"
              placeholder="持续时间，例如：3天、1周（首轮填写后无需重复）"
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
              disabled={loading}
            />
          </div>
        )}
        <div className="flex gap-3">
          <textarea
            className="input-field min-h-[52px] flex-1 resize-none py-3"
            placeholder={
              hasStarted
                ? '继续补充症状或回答 AI 的问题，按 Enter 快速发送...'
                : '请详细描述您的症状，按 Enter 快速发送...'
            }
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            rows={1}
            disabled={loading}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || loading}
            className="btn-primary shrink-0 self-end px-5 py-3.5"
          >
            {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : <Send className="h-5 w-5" />}
          </button>
        </div>
      </div>

      <MedicalDisclaimer className="mt-3 shrink-0" />

      <ConfirmDialog
        isOpen={showClearConfirm}
        onClose={() => setShowClearConfirm(false)}
        onConfirm={handleNewConversation}
        title="开始新对话"
        message="当前对话将被清空，AI 将不再拥有之前的上下文。确定要开始新对话吗？"
        confirmText="开始新对话"
        type="warning"
      />
    </div>
  );
}

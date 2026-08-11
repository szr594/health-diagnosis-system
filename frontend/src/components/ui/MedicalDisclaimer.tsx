import { AlertTriangle } from 'lucide-react';

export default function MedicalDisclaimer({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-start gap-2.5 rounded-xl border border-amber-500/20 bg-amber-500/8 px-4 py-2.5 text-xs text-amber-200/90 backdrop-blur-sm ${className}`}>
      <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-amber-400" />
      <p>AI 问诊仅供参考，不可替代执业医师诊断，请及时线下就医。</p>
    </div>
  );
}

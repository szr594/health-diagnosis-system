import { FileQuestion } from 'lucide-react';

interface EmptyStateProps {
  title?: string;
  description?: string;
  icon?: React.ElementType;
  action?: React.ReactNode;
}

export default function EmptyState({
  title = '暂无数据',
  description = '这里还没有内容',
  icon: Icon = FileQuestion,
  action,
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-14 text-center animate-fade-in">
      <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-white/[0.04]">
        <Icon className="h-8 w-8 text-white/30" />
      </div>
      <h3 className="text-base font-semibold text-white/70">{title}</h3>
      <p className="mt-1 max-w-xs text-sm text-white/40">{description}</p>
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

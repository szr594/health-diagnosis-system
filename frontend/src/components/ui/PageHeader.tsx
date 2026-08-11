import { ChevronRight } from 'lucide-react';
import { useLocation, Link } from 'react-router-dom';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  icon?: React.ElementType;
  breadcrumbs?: { label: string; to?: string }[];
}

export default function PageHeader({ title, subtitle, icon: Icon, breadcrumbs }: PageHeaderProps) {
  const loc = useLocation();
  const pMap: Record<string, string> = {
    '/dashboard': '工作台',
    '/consultation': 'AI 问诊',
    '/history': '问诊记录',
    '/knowledge': '健康知识库',
    '/profile': '个人中心',
  };

  const defBc = [{ label: '工作台', to: '/dashboard' }, { label: pMap[loc.pathname] || title }];
  const crumbs = breadcrumbs || defBc;

  return (
    <div className="mb-6 animate-fade-in">
      <nav className="mb-2 flex items-center gap-2 text-xs text-white/40">
        {crumbs.map((crumb, i) => (
          <span key={i} className="flex items-center gap-2">
            {crumb.to ? (
              <Link to={crumb.to} className="transition-colors hover:text-brand-300">{crumb.label}</Link>
            ) : (
              <span className="text-white/70">{crumb.label}</span>
            )}
            {i < crumbs.length - 1 && <ChevronRight className="h-3.5 w-3.5" />}
          </span>
        ))}
      </nav>
      <h1 className="page-title flex items-center gap-2.5">
        {Icon && <Icon className="h-7 w-7 text-brand-400" />}
        {title}
      </h1>
      {subtitle && <p className="page-subtitle">{subtitle}</p>}
    </div>
  );
}

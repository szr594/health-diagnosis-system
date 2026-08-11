import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../components/ui/Toast';
import { GlassCard } from '../components/ui/GlassCard';
import PageHeader from '../components/ui/PageHeader';
import Modal from '../components/ui/Modal';
import MedicalDisclaimer from '../components/ui/MedicalDisclaimer';
import { userApi } from '../services/api';
import { User, Phone, Calendar, Activity, Edit2, Check } from 'lucide-react';
import type { User as UserType } from '../types';

const commonAllergies = ['青霉素', '磺胺', '头孢', '花粉', '尘螨', '海鲜', '芒果', '花生', '碘', '乳胶'];
const commonDiseases = ['高血压', '糖尿病', '冠心病', '哮喘', '慢性胃炎', '乙肝', '甲状腺疾病', '高血脂', '痛风', '过敏性疾病'];
const genderOptions = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
];

export default function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const { showToast } = useToast();
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState<Partial<UserType>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  const openEdit = () => {
    setForm({
      nickname: user?.nickname || '',
      realName: user?.realName || '',
      phone: user?.phone || '',
      gender: user?.gender ?? 0,
      age: user?.age,
      height: user?.height,
      weight: user?.weight,
      allergyHistory: user?.allergyHistory || '',
      medicalHistory: user?.medicalHistory || '',
    });
    setErrors({});
    setIsEditOpen(true);
  };

  const validate = () => {
    const errs: Record<string, string> = {};
    if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
      errs.phone = '手机号格式不正确';
    }
    if (form.age != null && (form.age < 1 || form.age > 150)) {
      errs.age = '年龄范围 1-150';
    }
    if (form.height != null && (form.height <= 0 || form.height > 300)) {
      errs.height = '身高范围 1-300 cm';
    }
    if (form.weight != null && (form.weight <= 0 || form.weight > 500)) {
      errs.weight = '体重范围 1-500 kg';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      await userApi.updateProfile({
        nickname: form.nickname,
        realName: form.realName,
        phone: form.phone,
        gender: form.gender,
        age: form.age,
        height: form.height,
        weight: form.weight,
        allergyHistory: form.allergyHistory,
        medicalHistory: form.medicalHistory,
      });
      await refreshUser();
      showToast('保存成功', 'success');
      setIsEditOpen(false);
    } catch (err: any) {
      showToast(err.response?.data?.message || '保存失败', 'error');
    } finally {
      setSaving(false);
    }
  };

  const toggleTag = (type: 'allergyHistory' | 'medicalHistory', tag: string) => {
    const current = (form[type] || '').split(/[,，]/).map((s) => s.trim()).filter(Boolean);
    const exists = current.includes(tag);
    const next = exists ? current.filter((t) => t !== tag) : [...current, tag];
    setForm((f) => ({ ...f, [type]: next.join('，') }));
  };

  const genderLabel = (g?: number) => (g === 1 ? '男' : g === 2 ? '女' : '未知');
  const roleLabel = (r?: number) => (r === 2 ? '管理员' : r === 1 ? '医生' : '患者');

  const InfoItem = ({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) => (
    <div className="flex items-center gap-3">
      <Icon className="h-4 w-4 shrink-0 text-white/30" />
      <div>
        <p className="text-xs text-white/40">{label}</p>
        <p className="text-sm text-white/85">{value || '-'}</p>
      </div>
    </div>
  );

  return (
    <div className="space-y-6 animate-fade-in max-w-3xl">
      <PageHeader
        title="个人中心"
        subtitle="管理您的个人信息"
        icon={User}
        breadcrumbs={[{ label: '工作台', to: '/dashboard' }, { label: '个人中心' }]}
      />

      <GlassCard>
        <div className="flex items-start justify-between gap-4 sm:items-center">
          <div className="flex items-center gap-5">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-400 to-brand-600 text-2xl font-bold text-white shadow-glow">
              {user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U'}
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">{user?.nickname || user?.username}</h2>
              <p className="text-sm text-white/40">{roleLabel(user?.role)}</p>
            </div>
          </div>
          <button
            onClick={openEdit}
            className="btn-primary py-2.5 text-sm"
          >
            <Edit2 className="h-4 w-4" /> 编辑信息
          </button>
        </div>
      </GlassCard>

      <GlassCard>
        <h3 className="text-lg font-semibold text-white mb-5">基本信息</h3>
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <InfoItem icon={User} label="用户名" value={user?.username || ''} />
          <InfoItem icon={User} label="真实姓名" value={user?.realName || ''} />
          <InfoItem icon={Phone} label="手机号" value={user?.phone || ''} />
          <InfoItem icon={Calendar} label="年龄" value={user?.age ? `${user.age} 岁` : ''} />
          <InfoItem icon={User} label="性别" value={genderLabel(user?.gender)} />
          <InfoItem icon={Activity} label="身高/体重" value={user?.height && user?.weight ? `${user.height}cm / ${user.weight}kg` : ''} />
        </div>
      </GlassCard>

      <GlassCard>
        <h3 className="text-lg font-semibold text-white mb-5">健康档案</h3>
        <div className="space-y-5">
          <div>
            <p className="mb-2 text-sm font-medium text-white/60">过敏史</p>
            {user?.allergyHistory ? (
              <div className="flex flex-wrap gap-2">
                {user.allergyHistory.split(/[,，]/).map((s) => s.trim()).filter(Boolean).map((t) => (
                  <span key={t} className="badge badge-danger">{t}</span>
                ))}
              </div>
            ) : (
              <p className="text-sm text-white/40">无</p>
            )}
          </div>
          <div>
            <p className="mb-2 text-sm font-medium text-white/60">既往病史</p>
            {user?.medicalHistory ? (
              <div className="flex flex-wrap gap-2">
                {user.medicalHistory.split(/[,，]/).map((s) => s.trim()).filter(Boolean).map((t) => (
                  <span key={t} className="badge badge-warning">{t}</span>
                ))}
              </div>
            ) : (
              <p className="text-sm text-white/40">无</p>
            )}
          </div>
        </div>
      </GlassCard>

      <MedicalDisclaimer />

      <Modal
        isOpen={isEditOpen}
        onClose={() => setIsEditOpen(false)}
        title="编辑个人信息"
        width="lg"
        footer={
          <div className="flex justify-end gap-3">
            <button onClick={() => setIsEditOpen(false)} className="btn-secondary" disabled={saving}>取消</button>
            <button onClick={handleSave} disabled={saving} className="btn-primary">
              {saving ? (
                <span className="flex items-center gap-2">
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                  保存中...
                </span>
              ) : (
                <><Check className="h-4 w-4" /> 保存</>
              )}
            </button>
          </div>
        }
      >
        <div className="space-y-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="input-label">昵称</label>
              <input
                className="input-field"
                value={form.nickname || ''}
                onChange={(e) => setForm((f) => ({ ...f, nickname: e.target.value }))}
              />
            </div>
            <div>
              <label className="input-label">真实姓名</label>
              <input
                className="input-field"
                value={form.realName || ''}
                onChange={(e) => setForm((f) => ({ ...f, realName: e.target.value }))}
              />
            </div>
            <div>
              <label className="input-label">手机号</label>
              <input
                className={`input-field ${errors.phone ? 'border-red-500/50' : ''}`}
                value={form.phone || ''}
                onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
                placeholder="11位手机号"
              />
              {errors.phone && <p className="mt-1 text-xs text-red-400">{errors.phone}</p>}
            </div>
            <div>
              <label className="input-label">性别</label>
              <select
                className="input-field"
                value={form.gender || 0}
                onChange={(e) => setForm((f) => ({ ...f, gender: Number(e.target.value) }))}
              >
                {genderOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
            <div>
              <label className="input-label">年龄</label>
              <input
                type="number"
                className={`input-field ${errors.age ? 'border-red-500/50' : ''}`}
                value={form.age ?? ''}
                onChange={(e) => setForm((f) => ({ ...f, age: e.target.value ? Number(e.target.value) : undefined }))}
                placeholder="岁"
              />
              {errors.age && <p className="mt-1 text-xs text-red-400">{errors.age}</p>}
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="input-label">身高 (cm)</label>
                <input
                  type="number"
                  step="0.1"
                  className={`input-field ${errors.height ? 'border-red-500/50' : ''}`}
                  value={form.height ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, height: e.target.value ? Number(e.target.value) : undefined }))}
                />
                {errors.height && <p className="mt-1 text-xs text-red-400">{errors.height}</p>}
              </div>
              <div>
                <label className="input-label">体重 (kg)</label>
                <input
                  type="number"
                  step="0.1"
                  className={`input-field ${errors.weight ? 'border-red-500/50' : ''}`}
                  value={form.weight ?? ''}
                  onChange={(e) => setForm((f) => ({ ...f, weight: e.target.value ? Number(e.target.value) : undefined }))}
                />
                {errors.weight && <p className="mt-1 text-xs text-red-400">{errors.weight}</p>}
              </div>
            </div>
          </div>

          <div>
            <label className="input-label mb-2">过敏史（点击选择）</label>
            <div className="flex flex-wrap gap-2">
              {commonAllergies.map((tag) => {
                const active = ((form.allergyHistory || '').split(/[,，]/).map((s) => s.trim()).filter(Boolean)).includes(tag);
                return (
                  <button
                    key={tag}
                    type="button"
                    onClick={() => toggleTag('allergyHistory', tag)}
                    className={`rounded-full px-3 py-1 text-xs font-medium transition-all duration-200 ${
                      active
                        ? 'bg-red-500/18 text-red-200 border border-red-500/30'
                        : 'border border-white/[0.08] bg-white/[0.03] text-white/55 hover:bg-white/[0.06]'
                    }`}
                  >
                    {active && <Check className="inline h-3 w-3 mr-0.5" />}
                    {tag}
                  </button>
                );
              })}
            </div>
            <textarea
              className="input-field mt-3 min-h-[60px] resize-none"
              placeholder="其他过敏史，用逗号分隔"
              value={form.allergyHistory || ''}
              onChange={(e) => setForm((f) => ({ ...f, allergyHistory: e.target.value }))}
            />
          </div>

          <div>
            <label className="input-label mb-2">既往病史（点击选择）</label>
            <div className="flex flex-wrap gap-2">
              {commonDiseases.map((tag) => {
                const active = ((form.medicalHistory || '').split(/[,，]/).map((s) => s.trim()).filter(Boolean)).includes(tag);
                return (
                  <button
                    key={tag}
                    type="button"
                    onClick={() => toggleTag('medicalHistory', tag)}
                    className={`rounded-full px-3 py-1 text-xs font-medium transition-all duration-200 ${
                      active
                        ? 'bg-amber-500/18 text-amber-200 border border-amber-500/30'
                        : 'border border-white/[0.08] bg-white/[0.03] text-white/55 hover:bg-white/[0.06]'
                    }`}
                  >
                    {active && <Check className="inline h-3 w-3 mr-0.5" />}
                    {tag}
                  </button>
                );
              })}
            </div>
            <textarea
              className="input-field mt-3 min-h-[60px] resize-none"
              placeholder="其他既往病史，用逗号分隔"
              value={form.medicalHistory || ''}
              onChange={(e) => setForm((f) => ({ ...f, medicalHistory: e.target.value }))}
            />
          </div>
        </div>
      </Modal>
    </div>
  );
}

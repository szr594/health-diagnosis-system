import Modal from './Modal';

interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'warning' | 'info';
}

export default function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title = '确认操作',
  message = '您确定要执行此操作吗？',
  confirmText = '确认',
  cancelText = '取消',
  type = 'warning',
}: ConfirmDialogProps) {
  const cls = type === 'danger'
    ? 'btn-danger'
    : type === 'warning'
      ? 'btn-primary bg-gradient-to-r from-amber-500 to-amber-600 shadow-amber-500/25'
      : 'btn-primary';

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      footer={
        <div className="flex justify-end gap-3">
          <button onClick={onClose} className="btn-secondary">{cancelText}</button>
          <button onClick={() => { onConfirm(); onClose(); }} className={cls}>
            {confirmText}
          </button>
        </div>
      }
    >
      <p className="text-sm leading-relaxed text-white/70">{message}</p>
    </Modal>
  );
}

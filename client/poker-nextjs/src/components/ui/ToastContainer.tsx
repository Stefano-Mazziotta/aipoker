'use client';

import { Toast } from '@/hooks/useToast';

interface ToastContainerProps {
  toasts: Toast[];
  onRemove: (id: string) => void;
}

export function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 space-y-2 pointer-events-none">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`
            pointer-events-auto
            min-w-[300px] max-w-[500px]
            p-4 rounded-lg shadow-2xl
            transform transition-all duration-300 ease-out
            animate-slide-in-right
            ${getToastStyles(toast.type)}
          `}
          onClick={() => onRemove(toast.id)}
        >
          <div className="flex items-start gap-3">
            <span className="text-2xl shrink-0">
              {getToastIcon(toast.type)}
            </span>
            <p className="flex-1 font-medium">{toast.message}</p>
            <button
              onClick={(e) => {
                e.stopPropagation();
                onRemove(toast.id);
              }}
              className="text-white/80 hover:text-white transition-colors"
            >
              ✕
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

function getToastStyles(type: Toast['type']): string {
  switch (type) {
    case 'success':
      return 'bg-green-600 text-white border border-green-400';
    case 'warning':
      return 'bg-yellow-600 text-white border border-yellow-400';
    case 'error':
      return 'bg-red-600 text-white border border-red-400';
    default:
      return 'bg-blue-600 text-white border border-blue-400';
  }
}

function getToastIcon(type: Toast['type']): string {
  switch (type) {
    case 'success':
      return '✅';
    case 'warning':
      return '⚠️';
    case 'error':
      return '❌';
    default:
      return 'ℹ️';
  }
}

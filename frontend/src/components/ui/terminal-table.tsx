import { cn } from '@/lib/utils';

interface TerminalTableProps {
  title: string;
  headers: string[];
  children: React.ReactNode;
  className?: string;
}

export function TerminalTable({ title, headers, children, className }: TerminalTableProps) {
  return (
    <div className={cn('overflow-hidden rounded-lg border border-border-hover bg-background shadow-xl', className)}>
      {/* Terminal-style header */}
      <div className="flex items-center gap-2 border-b border-border-hover bg-surface px-4 py-2">
        <div className="flex gap-1.5">
          <div className="h-2 w-2 rounded-full bg-red-500/60" />
          <div className="h-2 w-2 rounded-full bg-yellow-500/60" />
          <div className="h-2 w-2 rounded-full bg-primary/60" />
        </div>
        <div className="font-mono text-sm text-text-secondary">{title}</div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-border bg-surface/50">
              {headers.map((header, i) => (
                <th
                  key={i}
                  className="px-4 py-2 text-left font-mono text-xs uppercase tracking-wider text-text-muted"
                >
                  {header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border/50">
            {children}
          </tbody>
        </table>
      </div>
    </div>
  );
}

interface TerminalTableRowProps {
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
}

export function TerminalTableRow({ children, onClick, className }: TerminalTableRowProps) {
  return (
    <tr
      onClick={onClick}
      className={cn(
        'font-mono text-sm transition-colors',
        onClick && 'cursor-pointer hover:bg-surface-elevated/50',
        className
      )}
    >
      {children}
    </tr>
  );
}

interface TerminalTableCellProps {
  children: React.ReactNode;
  className?: string;
}

export function TerminalTableCell({ children, className }: TerminalTableCellProps) {
  return (
    <td className={cn('px-4 py-3', className)}>
      {children}
    </td>
  );
}


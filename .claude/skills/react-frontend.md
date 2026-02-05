Helps with React frontend development following Algorena's terminal-inspired design system and patterns.

**Usage:** `/react-frontend <task description>`

**Examples:**
- `/react-frontend add leaderboard page`
- `/react-frontend create tournament card component`
- `/react-frontend add filter dropdown to matches`

**What this skill does:**

## Project Understanding
- React 19 with TypeScript and Vite
- Dark-only terminal-inspired UI (zinc palette, emerald accents)
- TanStack Query for server state management
- Auto-generated API client from OpenAPI spec (`bun run api:generate`)
- Tailwind CSS v4 with custom utilities
- Lucide icons for all iconography
- i18next for internationalization

## Design System - Terminal Aesthetic

**Color Palette:**
- Background: `bg-zinc-950`, `bg-zinc-900`
- Borders: `border-zinc-700`, `border-zinc-800`
- Text primary: `text-zinc-100`
- Text secondary: `text-zinc-400`, `text-zinc-500`
- Accent: `text-emerald-500`, `bg-emerald-600`
- Error: `text-red-400`, `bg-red-600`
- Success: `text-emerald-500`
- Warning: `text-yellow-500`

**Typography:**
- **Always use `font-mono`** for terminal feel
- Headers: Terminal-style commands (e.g., `$ ./bots --list`)
- Subheaders: Commented descriptions (e.g., `# Manage your battle bots`)
- Uppercase text for status labels (e.g., `ACTIVE`, `LIVE`, `FINISHED`)

**Terminal UI Components:**

1. **TerminalTable** - Primary data display component
   ```tsx
   <TerminalTable
     title="bots.db"           // File name in terminal header
     headers={['status', 'name', 'game', 'actions']}
   >
     <TerminalTableRow onClick={() => {}}>  {/* Optional click handler */}
       <TerminalTableCell>Content</TerminalTableCell>
     </TerminalTableRow>
   </TerminalTable>
   ```
   - Features macOS-style traffic lights (red/yellow/green dots)
   - Terminal header bar with file name
   - Hover effect on clickable rows: `hover:bg-zinc-800/50`

2. **Page Headers** - Terminal command style
   ```tsx
   <h1 className="font-mono text-2xl font-bold text-emerald-500">
     $ ./bots --list
   </h1>
   <p className="mt-1 font-mono text-sm text-zinc-500">
     # Manage your battle bots
   </p>
   ```

3. **Empty States** - Command-line themed
   ```tsx
   <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-800 py-16 text-center">
     <Bot className="h-12 w-12 text-zinc-600" />
     <h3 className="mt-4 font-mono text-lg font-semibold">No bots found</h3>
     <p className="mt-1 font-mono text-sm text-zinc-500">
       $ create your first bot to start competing
     </p>
     <Button onClick={handleCreate} className="mt-6 gap-2 font-mono">
       <Plus className="h-4 w-4" />
       new bot
     </Button>
   </div>
   ```

4. **Status Indicators**
   ```tsx
   {/* Circle indicator with color */}
   <Circle className="h-2 w-2 fill-emerald-500 text-emerald-500" />

   {/* With label */}
   <div className="flex items-center gap-2">
     <Circle className={cn('h-2 w-2', active ? 'fill-emerald-500 text-emerald-500' : 'fill-zinc-600 text-zinc-600')} />
     <span className={cn('text-xs', active ? 'text-emerald-500' : 'text-zinc-600')}>
       {active ? 'ACTIVE' : 'IDLE'}
     </span>
   </div>
   ```

5. **Badges/Tags** - Rounded with dark background
   ```tsx
   <span className="rounded bg-zinc-800 px-2 py-0.5 text-xs text-zinc-400">
     CHESS
   </span>
   ```

## Component Patterns

**File Structure:**
```
frontend/src/
├── routes/              # Page components (HomePage.tsx, BotsPage.tsx)
├── features/            # Feature modules with colocation
│   └── {feature}/
│       ├── use{Feature}.ts        # TanStack Query hooks
│       ├── {Feature}Dialog.tsx    # Dialogs for CRUD
│       └── components/            # Feature-specific components
├── components/
│   └── ui/              # Reusable UI components (shadcn-style)
└── api/
    └── generated/       # Auto-generated API client (DO NOT edit)
```

**Page Component Pattern:**
```tsx
export function BotsPage() {
  const { isAuthenticated, login } = useAuth();
  const { data: botsPage, isLoading, error } = useBots();
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  // Early return for unauthenticated
  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <Bot className="h-16 w-16 text-zinc-600" />
        <h2 className="mt-4 text-xl font-semibold">{t('errors.unauthorized')}</h2>
        <Button onClick={login}>{t('nav.login')}</Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with terminal command */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-mono text-2xl font-bold text-emerald-500">
            $ ./bots --list
          </h1>
          <p className="mt-1 font-mono text-sm text-zinc-500">
            # Manage your battle bots
          </p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2 font-mono">
          <Plus className="h-4 w-4" />
          new bot
        </Button>
      </div>

      {/* Loading/Error/Empty/Data states */}
      {isLoading ? <LoadingState /> : error ? <ErrorState /> : data.length === 0 ? <EmptyState /> : <DataTable />}
    </div>
  );
}
```

**TanStack Query Hooks Pattern:**
```tsx
// Query key factory
export const botKeys = {
  all: ['bots'] as const,
  lists: () => [...botKeys.all, 'list'] as const,
  list: (filters: Record<string, unknown>) => [...botKeys.lists(), filters] as const,
  details: () => [...botKeys.all, 'detail'] as const,
  detail: (id: number) => [...botKeys.details(), id] as const,
  stats: (id: number) => [...botKeys.all, 'stats', id] as const,
};

// Query hook
export function useBots() {
  return useQuery({
    queryKey: botKeys.lists(),
    queryFn: async () => {
      const response = await getBots();  // Auto-generated API client
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bots');
      }
      return response.data;
    },
  });
}

// Mutation hook
export function useCreateBot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: CreateBotRequest) => {
      const response = await createBot({ body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to create bot');
      }
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: botKeys.lists() });
    },
  });
}
```

**Dialog Pattern:**
```tsx
export function CreateBotDialog({ open, onOpenChange }: DialogProps) {
  const [name, setName] = useState('');
  const createBot = useCreateBot();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createBot.mutateAsync({ name /* ... */ });
      // Reset form
      setName('');
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to create:', error);
    }
  };

  const handleClose = () => {
    // Reset form state
    setName('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create Bot</DialogTitle>
          <DialogDescription>Description here</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="name">Name *</Label>
            <Input
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <DialogFooter className="pt-4">
            <Button type="button" variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={createBot.isPending}>
              {createBot.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
```

## UI Components (Shadcn-style)

All UI components use:
- Class Variance Authority (CVA) for variants
- `cn()` utility for className merging
- TypeScript with proper prop types
- `forwardRef` for ref forwarding

**Button:**
```tsx
<Button variant="default">Default (emerald)</Button>
<Button variant="outline">Outline</Button>
<Button variant="ghost">Ghost</Button>
<Button variant="destructive">Destructive (red)</Button>
<Button size="sm" className="gap-2 font-mono">
  <Plus className="h-4 w-4" />
  new bot
</Button>
```

**Form Components:**
```tsx
{/* Always wrap in div with space-y-2 */}
<div className="space-y-2">
  <Label htmlFor="field">Field Name</Label>
  <Input
    id="field"
    value={value}
    onChange={(e) => setValue(e.target.value)}
    placeholder="Placeholder"
    required
  />
  <p className="text-xs text-zinc-500">Helper text</p>
</div>

{/* Select */}
<Select value={game} onChange={(e) => setGame(e.target.value)}>
  <option value="CHESS">Chess</option>
  <option value="CONNECT_FOUR">Connect 4</option>
</Select>

{/* Textarea */}
<Textarea
  value={description}
  onChange={(e) => setDescription(e.target.value)}
  rows={3}
  maxLength={500}
/>
```

## Icons (Lucide)

Common icons and their usage:
- `Plus` - Create actions
- `Edit2` - Edit actions
- `Trash2` - Delete actions
- `Eye` - View details
- `Loader2` - Loading spinner (with `animate-spin`)
- `Circle` - Status indicators (use `fill-` and `text-` for color)
- `Bot` - Bot-related features
- `Swords` - Matches/battles
- `Filter` - Filter controls
- `Clock`, `PlayCircle`, `CheckCircle`, `XCircle` - Match statuses

Size classes: `h-4 w-4` (default), `h-3.5 w-3.5` (small), `h-12 w-12` (large)

## Common Utilities

**Date Formatting:**
```tsx
// Relative time for recent items
const formatDate = (dateString?: string) => {
  if (!dateString) return '—';
  const date = new Date(dateString);
  const now = new Date();
  const diffMins = Math.floor((now.getTime() - date.getTime()) / 60000);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMins < 1) return 'just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;

  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric'
  });
};

// Standard date format
const formatDate = (dateString?: string) => {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: '2-digit'
  });
};
```

**Filtering Pattern:**
```tsx
const [statusFilter, setStatusFilter] = useState<'all' | 'active'>('all');
const [searchQuery, setSearchQuery] = useState('');

const filteredData = useMemo(() => {
  if (!data) return [];

  return data.filter(item => {
    if (statusFilter === 'active' && !item.active) return false;
    if (searchQuery && !item.name.toLowerCase().includes(searchQuery.toLowerCase())) return false;
    return true;
  });
}, [data, statusFilter, searchQuery]);
```

## API Client Integration

**Auto-generated client:**
- NEVER edit files in `api/generated/`
- Run `bun run api:generate` after backend OpenAPI spec changes
- Import types and functions from `@/api/generated`

**Usage:**
```tsx
import { getBots, createBot } from '@/api/generated';
import type { BotDto, CreateBotRequest } from '@/api/generated';

// In TanStack Query hook
const response = await getBots();
if (response.error) {
  throw new Error(response.error.message || 'Failed to fetch');
}
return response.data;

// With path parameters
const response = await getBotStats({ path: { botId } });

// With body
const response = await createBot({ body: data });
```

## Styling Guidelines

**Spacing:**
- Page wrapper: `<div className="space-y-6">`
- Form fields: `<div className="space-y-2">`
- Button groups: `gap-2`, `gap-4`

**Text Styling:**
- Always use `font-mono` for terminal aesthetic
- Headers: `text-2xl font-bold text-emerald-500`
- Subtext: `text-sm text-zinc-500`
- Labels: `text-xs uppercase tracking-wider text-zinc-500`

**Interactive Elements:**
- Hover effects: `hover:bg-zinc-800`, `hover:bg-zinc-800/50` (with opacity)
- Transitions: `transition-colors`
- Disabled: `disabled:opacity-50 disabled:pointer-events-none`

**Borders & Backgrounds:**
- Cards: `rounded-lg border border-zinc-700 bg-zinc-950`
- Subtle dividers: `border-zinc-800/50`, `divide-zinc-800/50`
- Focus rings: `focus-visible:ring-1 focus-visible:ring-zinc-300`

## Commands
Frontend commands (from `frontend/` directory):
- `bun install` - Install dependencies
- `bun run dev` - Start dev server (port 5173)
- `bun run build` - Production build
- `bun run lint` - Run ESLint
- `bun run api:generate` - Regenerate API client from OpenAPI spec

## What to Ask
Before generating code:
1. Is this a new page or component? Where should it live?
2. Does it need data fetching? What API endpoints?
3. Are there filters, search, or pagination requirements?
4. Should it be accessible without authentication?
5. Does it need i18n keys added?

## Important Notes
- Use Bun, not npm/yarn/pnpm
- Dark mode only - no light mode variants
- All text should use `font-mono` unless there's a specific reason not to
- Status/state text should be UPPERCASE
- Empty states should feel like terminal prompts
- Dialogs should have proper form reset on close
- Always handle loading, error, and empty states
- Use `cn()` for conditional className logic
- API client is auto-generated - regenerate after backend changes

## Tech Stack
- React 19 with TypeScript
- Vite for build tooling
- Bun for package management
- TanStack Query v5 for server state
- Tailwind CSS v4 for styling
- Lucide React for icons
- i18next for internationalization
- @hey-api/openapi-ts for API client generation

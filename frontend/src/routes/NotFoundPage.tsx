import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Terminal } from 'lucide-react';

export function NotFoundPage() {

  return (
    <div className="flex items-center justify-center py-16">
      <div className="flex flex-col rounded-lg border border-dashed border-border p-8">
        <div className="flex items-center gap-2">
          <Terminal className="h-6 w-6 text-primary" />
          <h1 className="font-mono text-xl font-semibold text-primary">
            $ command not found
          </h1>
        </div>
        <p className="mt-4 font-mono text-sm text-text-muted">
          # The page you're looking for doesn't exist
        </p>
        <Link to="/" className="mt-6">
          <Button className="gap-2 font-mono">
            cd ~/home
          </Button>
        </Link>
      </div>
    </div>
  );
}


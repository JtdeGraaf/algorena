import { Outlet } from 'react-router-dom';
import { Navbar } from '@/components/Navbar';

export function Layout() {
  return (
    <div className="flex min-h-screen flex-col bg-zinc-950 text-zinc-100">
      <Navbar />
      <main className="container mx-auto flex-1 px-4 py-8">
        <Outlet />
      </main>
      <footer className="border-t border-zinc-800 py-6">
        <div className="container mx-auto px-4 text-center text-sm text-zinc-500">
          <p>Algorena © {new Date().getFullYear()} — Bot Battle Arena</p>
        </div>
      </footer>
    </div>
  );
}


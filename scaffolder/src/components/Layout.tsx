import React, { useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FileText, Database, Layers } from 'lucide-react';

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const shouldWarnBeforeUnload =
    location.pathname === '/project' || location.pathname === '/entities' || location.pathname === '/stack';

  useEffect(() => {
    if (!shouldWarnBeforeUnload) return;

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      event.preventDefault();
      event.returnValue = 'Reloading will clear your current configuration.';
      return event.returnValue;
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [shouldWarnBeforeUnload]);

  const navItems = [
    { type: 'link' as const, path: '/project', label: 'Project', icon: FileText },
    { type: 'link' as const, path: '/entities', label: 'Entities', icon: Database },
    { type: 'link' as const, path: '/stack', label: 'Stack', icon: Layers },
  ];

  return (
    <div className="flex h-screen bg-gray-100">
      <aside className="w-64 bg-white border-r border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-xl font-bold text-gray-800 flex items-center gap-2">
             Scaffolder
          </h1>
        </div>
        <nav className="p-4 space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = item.type === 'link' && location.pathname === item.path;
            const className = `flex w-full items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
              isActive ? 'bg-blue-50 text-blue-700 font-medium' : 'text-gray-600 hover:bg-gray-50'
            }`;

            return (
              <Link
                key={item.path}
                to={item.path}
                className={className}
              >
                <Icon size={20} />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>
      <main className="flex-1 overflow-auto p-8">
        <div className="max-w-6xl mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
};

import { useEffect } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { getStackOptions } from './api/client';
import { Layout } from './components/Layout';
import { Entities } from './pages/Entities';
import { Project } from './pages/Project';
import { Stack } from './pages/Stack';
import { useProjectStore } from './store/projectStore';

function App() {
  const setStack = useProjectStore((s) => s.setStack);
  const setStackOptions = useProjectStore((s) => s.setStackOptions);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      try {
        const stackOptions = await getStackOptions();
        if (cancelled) return;
        setStackOptions(stackOptions);

        const current = useProjectStore.getState().config.stack;
        const patch: Partial<typeof current> = {};

        const inList = (list: Array<{ id: string }>, value: string | undefined) =>
          value !== undefined && list.some((o) => o.id === value);

        if (!inList(stackOptions.databases, current.database)) {
          const next = stackOptions.defaults.database ?? stackOptions.databases[0]?.id;
          if (next) patch.database = next as typeof current.database;
        }
        if (!inList(stackOptions.authentication, current.authentication)) {
          const next = stackOptions.defaults.authentication ?? stackOptions.authentication[0]?.id;
          if (next) patch.authentication = next as typeof current.authentication;
        }
        if (!inList(stackOptions.migration, current.migration)) {
          const next = stackOptions.defaults.migration ?? stackOptions.migration[0]?.id;
          if (next) patch.migration = next as typeof current.migration;
        }
        if (!inList(stackOptions.cicd, current.ci)) {
          const next = stackOptions.defaults.ci ?? stackOptions.cicd[0]?.id;
          if (next) patch.ci = next as typeof current.ci;
        }

        const normalize = (arr: string[] | undefined) => JSON.stringify([...(arr ?? [])].slice().sort());
        const allowedModes = new Set(stackOptions.deployment.map((m) => normalize(m.deployment)));
        if (!allowedModes.has(normalize(current.deployment))) {
          const next = stackOptions.defaults.deployment ?? stackOptions.deployment[0]?.deployment ?? [];
          patch.deployment = next as typeof current.deployment;
        }

        if (Object.keys(patch).length > 0) {
          setStack(patch);
        }
      } catch {
        if (cancelled) return;
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [setStack, setStackOptions]);

  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Navigate to="/project" replace />} />
          <Route path="/project" element={<Project />} />
          <Route path="/stack" element={<Stack />} />
          <Route path="/entities" element={<Entities />} />
          <Route path="*" element={<Navigate to="/project" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;

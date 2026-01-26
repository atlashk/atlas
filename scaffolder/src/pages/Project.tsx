import React from 'react';
import { useProjectStore } from '../store/projectStore';
import { GeneralSection } from './project/GeneralSection';

export const Project: React.FC = () => {
  const { config, setProjectInfo } = useProjectStore();

  return (
    <div className="space-y-8">
      <GeneralSection config={config} setProjectInfo={setProjectInfo} />
    </div>
  );
};

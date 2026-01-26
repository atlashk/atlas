import React from 'react';
import { ProjectConfig } from '../../models/types';
import { textInputClassName } from './ui';

type GeneralSectionProps = {
  config: ProjectConfig;
  setProjectInfo: (info: Partial<ProjectConfig>) => void;
};

export const GeneralSection: React.FC<GeneralSectionProps> = ({ config, setProjectInfo }) => {
  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-800 mb-2">General</h2>
      <div className="grid grid-cols-2 gap-4 bg-white p-6 rounded-xl border border-gray-200">
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">Project Name</label>
          <input
            type="text"
            value={config.projectName}
            onChange={(e) => setProjectInfo({ projectName: e.target.value })}
            className={textInputClassName}
          />
        </div>
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <input
            type="text"
            value={config.description}
            onChange={(e) => setProjectInfo({ description: e.target.value })}
            className={textInputClassName}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Group ID</label>
          <input
            type="text"
            value={config.groupId}
            onChange={(e) => setProjectInfo({ groupId: e.target.value })}
            className={textInputClassName}
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Artifact ID</label>
          <input
            type="text"
            value={config.artifactId}
            onChange={(e) => setProjectInfo({ artifactId: e.target.value })}
            className={textInputClassName}
          />
        </div>
        <div className="col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">Base Package</label>
          <input
            type="text"
            value={config.basePackage}
            onChange={(e) => setProjectInfo({ basePackage: e.target.value })}
            className={textInputClassName}
          />
        </div>
      </div>
    </div>
  );
};


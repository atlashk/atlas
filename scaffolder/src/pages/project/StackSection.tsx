import React, { useMemo } from 'react';
import { DeploymentType, JwtConfig, StackConfig, StackOptionsResponse } from '../../models/types';
import { cardClassName } from './ui';
import { JwtSettings } from './JwtSettings';

const Card: React.FC<React.PropsWithChildren> = ({ children }) => {
  return <div className={cardClassName}>{children}</div>;
};

type RadioOption = { id: string; label: string };

type StackOptionGroupProps = {
  title: string;
  name: string;
  value: string;
  options: RadioOption[];
  colorClassName: string;
  onChange: (nextId: string) => void;
  children?: React.ReactNode;
};

const StackOptionGroup: React.FC<StackOptionGroupProps> = ({
  title,
  name,
  value,
  options,
  colorClassName,
  onChange,
  children,
}) => {
  return (
    <Card>
      <div className="space-y-3">
        <h3 className="font-semibold text-lg">{title}</h3>
        <div className="flex flex-wrap gap-x-6 gap-y-3">
          {options.map((opt) => (
            <label key={opt.id} className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name={name}
                checked={value === opt.id}
                onChange={() => onChange(opt.id)}
                className={colorClassName}
              />
              <span>{opt.label}</span>
            </label>
          ))}
        </div>
        {children}
      </div>
    </Card>
  );
};

const normalizeDeployment = (deployment: readonly DeploymentType[]) => [...deployment].slice().sort().join('|');

type DeploymentGroupProps = {
  options: StackOptionsResponse['deployment'];
  current: DeploymentType[];
  setStack: (stack: Partial<StackConfig>) => void;
};

const DeploymentGroup: React.FC<DeploymentGroupProps> = ({ options, current, setStack }) => {
  const currentDeploymentKey = useMemo(() => normalizeDeployment(current ?? []), [current]);

  return (
    <Card>
      <div className="space-y-3">
        <h3 className="font-semibold text-lg">Deployment</h3>
        <div className="flex flex-wrap gap-x-6 gap-y-3">
          {options.map((opt) => {
            const checked = normalizeDeployment(opt.deployment) === currentDeploymentKey;
            return (
              <label key={opt.id} className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="deployment"
                  checked={checked}
                  onChange={() => setStack({ deployment: opt.deployment })}
                  onClick={() => {
                    if (checked) setStack({ deployment: [] });
                  }}
                  className="text-orange-600 focus:ring-orange-500"
                />
                <span>{opt.label}</span>
              </label>
            );
          })}
        </div>
      </div>
    </Card>
  );
};

type StackSectionProps = {
  stack: StackConfig;
  stackOptions: StackOptionsResponse | null;
  isLoadingStackOptions: boolean;
  stackOptionsError: string | null;
  onRetryLoadStackOptions: () => void;
  setStack: (stack: Partial<StackConfig>) => void;
  jwt: JwtConfig;
  setJwtConfig: (updater: (current: JwtConfig) => JwtConfig) => void;
};

export const StackSection: React.FC<StackSectionProps> = ({
  stack,
  stackOptions,
  isLoadingStackOptions,
  stackOptionsError,
  onRetryLoadStackOptions,
  setStack,
  jwt,
  setJwtConfig,
}) => {
  return (
    <div>
      <div className="flex flex-wrap gap-2 mb-2">
        <h2 className="text-2xl font-bold text-gray-800">Stack</h2>
      </div>

      {!stackOptions && (isLoadingStackOptions || stackOptionsError) && (
        <Card>
          <div className="flex items-center justify-between gap-4">
            <div className="text-sm text-gray-700">
              {isLoadingStackOptions ? 'Loading stack options...' : stackOptionsError}
            </div>
            {!isLoadingStackOptions && (
              <button
                type="button"
                className="px-3 py-2 rounded-md border border-gray-200 text-sm hover:bg-gray-50"
                onClick={() => onRetryLoadStackOptions()}
              >
                Retry
              </button>
            )}
          </div>
        </Card>
      )}

      {!stackOptions && !isLoadingStackOptions && !stackOptionsError && (
        <Card>
          <div className="text-sm text-gray-700">Initializing...</div>
        </Card>
      )}

      {stackOptions && (
        <div className="space-y-4">
          <StackOptionGroup
            title="Database"
            name="database"
            value={stack.database}
            options={stackOptions.databases}
            colorClassName="text-blue-600 focus:ring-blue-500"
            onChange={(id) => setStack({ database: id })}
          />

          <StackOptionGroup
            title="Authentication"
            name="auth"
            value={stack.authentication}
            options={stackOptions.authentication}
            colorClassName="text-purple-600 focus:ring-purple-500"
            onChange={(id) => setStack({ authentication: id })}
          >
            {stack.authentication === 'jwt' && <JwtSettings jwt={jwt} setJwtConfig={setJwtConfig} />}
          </StackOptionGroup>

          <StackOptionGroup
            title="Migration"
            name="migration"
            value={stack.migration}
            options={stackOptions.migration}
            colorClassName="text-green-600 focus:ring-green-500"
            onChange={(id) => setStack({ migration: id })}
          />

          <DeploymentGroup options={stackOptions.deployment} current={stack.deployment} setStack={setStack} />

          <StackOptionGroup
            title="CI / CD"
            name="ci"
            value={stack.ci}
            options={stackOptions.cicd}
            colorClassName="text-red-600 focus:ring-red-500"
            onChange={(id) => setStack({ ci: id })}
          />
        </div>
      )}
    </div>
  );
};


import React, { useCallback, useMemo, useState } from 'react';
import { JwtClientTokenTransport, JwtConfig, JwtSigningAlgorithm } from '../../models/types';
import { useProjectStore } from '../../store/projectStore';
import { RequiredAsterisk, textInputClassName, TtlUnit } from './ui';

type JwtSettingsProps = {
  jwt: JwtConfig;
  setJwtConfig: (updater: (current: JwtConfig) => JwtConfig) => void;
};

export const JwtSettings: React.FC<JwtSettingsProps> = ({ jwt, setJwtConfig }) => {
  const entities = useProjectStore((s) => s.config.entities);
  const [roleDraft, setRoleDraft] = useState('');
  const [roleError, setRoleError] = useState<string | null>(null);
  const [accessTtlUnit, setAccessTtlUnit] = useState<TtlUnit>('hours');
  const [refreshTtlUnit, setRefreshTtlUnit] = useState<TtlUnit>('days');

  const unitSeconds = useMemo(() => ({ seconds: 1, minutes: 60, hours: 3600, days: 86400 }) as const, []);
  const normalizeValue = useCallback((value: string) => value.trim().toLowerCase(), []);

  const entityOptions = useMemo(() => entities.map((e) => e.name), [entities]);
  const fieldOptionsByEntity = useMemo(() => {
    const map = new Map<string, string[]>();
    for (const entity of entities) {
      map.set(entity.name, (entity.fields ?? []).map((f) => f.name));
    }
    return map;
  }, [entities]);

  const updateFieldMapping = useCallback(
    (
      key: keyof JwtConfig['fieldMapping'],
      patch: Partial<JwtConfig['fieldMapping'][keyof JwtConfig['fieldMapping']]>,
    ) => {
      setJwtConfig((current) => ({
        ...current,
        fieldMapping: {
          ...current.fieldMapping,
          [key]: {
            ...current.fieldMapping[key],
            ...patch,
          },
        },
      }));
    },
    [setJwtConfig],
  );

  const accessTtlValue = useMemo(() => {
    return jwt.accessTokenTtlSeconds / unitSeconds[accessTtlUnit];
  }, [accessTtlUnit, jwt.accessTokenTtlSeconds, unitSeconds]);

  const refreshTtlValue = useMemo(() => {
    return jwt.refreshTokenTtlSeconds / unitSeconds[refreshTtlUnit];
  }, [jwt.refreshTokenTtlSeconds, refreshTtlUnit, unitSeconds]);

  const addRole = useCallback(() => {
    const raw = roleDraft.trim();
    if (!raw) return;
    const normalized = raw.toUpperCase();
    if (!/^[A-Z][A-Z0-9_]*$/.test(normalized)) {
      setRoleError('Role must start with a letter and contain only A-Z, 0-9, _');
      return;
    }
    setRoleError(null);
    setJwtConfig((current) => {
      if (current.roles.includes(normalized)) return current;
      return { ...current, roles: [...current.roles, normalized] };
    });
    setRoleDraft('');
  }, [roleDraft, setJwtConfig]);

  const removeRole = useCallback(
    (role: string) => {
      setJwtConfig((current) => ({ ...current, roles: current.roles.filter((r) => r !== role) }));
    },
    [setJwtConfig],
  );

  const updateTransport = useCallback(
    (next: JwtClientTokenTransport) => {
      setJwtConfig((current) => ({ ...current, clientTokenTransport: next }));
    },
    [setJwtConfig],
  );

  const updateSigningAlgorithm = useCallback(
    (alg: JwtSigningAlgorithm) => {
      setJwtConfig((current) => ({
        ...current,
        signingAlgorithm: alg,
        hs256Secret: alg === 'HS256' ? current.hs256Secret ?? '' : undefined,
      }));
    },
    [setJwtConfig],
  );

  return (
    <div className="pt-5 mt-5 border-t border-gray-200">
      <div className="space-y-6">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <h4 className="font-semibold text-base">JWT Settings</h4>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Client token transport <RequiredAsterisk />
            </label>
            <div className="flex flex-wrap gap-6">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="jwt-transport"
                  checked={jwt.clientTokenTransport.type === 'authorization_header'}
                  onChange={() =>
                    updateTransport({
                      type: 'authorization_header',
                      headerName:
                        jwt.clientTokenTransport.type === 'authorization_header'
                          ? jwt.clientTokenTransport.headerName
                          : 'Authorization',
                      prefix:
                        jwt.clientTokenTransport.type === 'authorization_header'
                          ? jwt.clientTokenTransport.prefix
                          : 'Bearer ',
                    })
                  }
                  className="text-blue-600 focus:ring-blue-500"
                />
                <span>Authorization header</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  name="jwt-transport"
                  checked={jwt.clientTokenTransport.type === 'cookie'}
                  onChange={() =>
                    updateTransport({
                      type: 'cookie',
                      accessTokenCookieName:
                        jwt.clientTokenTransport.type === 'cookie'
                          ? jwt.clientTokenTransport.accessTokenCookieName
                          : 'accessToken',
                      refreshTokenCookieName:
                        jwt.clientTokenTransport.type === 'cookie'
                          ? jwt.clientTokenTransport.refreshTokenCookieName
                          : 'refreshToken',
                    })
                  }
                  className="text-blue-600 focus:ring-blue-500"
                />
                <span>Cookie</span>
              </label>
            </div>
          </div>

          <div className="col-span-2 grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Access token TTL <RequiredAsterisk />
              </label>
              <div className="flex gap-2">
                <input
                  type="number"
                  min={0}
                  value={Number.isFinite(accessTtlValue) ? String(accessTtlValue) : ''}
                  onChange={(e) => {
                    const next = Number(e.target.value);
                    if (!Number.isFinite(next) || next <= 0) return;
                    setJwtConfig((current) => ({
                      ...current,
                      accessTokenTtlSeconds: Math.round(next * unitSeconds[accessTtlUnit]),
                    }));
                  }}
                  className={textInputClassName}
                />
                <select
                  value={accessTtlUnit}
                  onChange={(e) => setAccessTtlUnit(e.target.value as TtlUnit)}
                  className="px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                >
                  {(['seconds', 'minutes', 'hours', 'days'] as const).map((u) => (
                    <option key={u} value={u}>
                      {u}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Refresh token TTL <RequiredAsterisk />
              </label>
              <div className="flex gap-2">
                <input
                  type="number"
                  min={0}
                  value={Number.isFinite(refreshTtlValue) ? String(refreshTtlValue) : ''}
                  onChange={(e) => {
                    const next = Number(e.target.value);
                    if (!Number.isFinite(next) || next <= 0) return;
                    setJwtConfig((current) => ({
                      ...current,
                      refreshTokenTtlSeconds: Math.round(next * unitSeconds[refreshTtlUnit]),
                    }));
                  }}
                  className={textInputClassName}
                />
                <select
                  value={refreshTtlUnit}
                  onChange={(e) => setRefreshTtlUnit(e.target.value as TtlUnit)}
                  className="px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                >
                  {(['seconds', 'minutes', 'hours', 'days'] as const).map((u) => (
                    <option key={u} value={u}>
                      {u}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Field mapping <RequiredAsterisk />
            </label>
            {entities.length === 0 ? (
              <div className="text-sm text-gray-500">Create at least one entity with fields first.</div>
            ) : (
              <div className="space-y-3">
                {(
                  [
                    { key: 'username', label: 'Username' },
                    { key: 'password', label: 'Password' },
                    { key: 'role', label: 'Role' },
                  ] as const
                ).map(({ key, label }) => {
                  const currentEntity = jwt.fieldMapping[key].entityName;
                  const currentField = jwt.fieldMapping[key].fieldName;

                  const resolvedEntityName =
                    entityOptions.find((n) => normalizeValue(n) === normalizeValue(currentEntity)) ?? currentEntity;
                  const fieldOptions =
                    fieldOptionsByEntity.get(
                      entityOptions.find((n) => normalizeValue(n) === normalizeValue(resolvedEntityName)) ??
                        resolvedEntityName,
                    ) ?? [];
                  const resolvedFieldName =
                    fieldOptions.find((n) => normalizeValue(n) === normalizeValue(currentField)) ?? currentField;

                  const entityMissing = !resolvedEntityName?.trim();
                  const fieldMissing = !!resolvedEntityName?.trim() && !resolvedFieldName?.trim();

                  return (
                    <div key={key} className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">{label} entity</label>
                        <select
                          value={resolvedEntityName}
                          required
                          onChange={(e) => updateFieldMapping(key, { entityName: e.target.value, fieldName: '' })}
                          className={textInputClassName}
                        >
                          {resolvedEntityName &&
                          !entityOptions.some((name) => normalizeValue(name) === normalizeValue(resolvedEntityName)) ? (
                            <option value={resolvedEntityName}>{resolvedEntityName}</option>
                          ) : null}
                          <option value="" disabled>
                            Select an entity
                          </option>
                          {entityOptions.map((name) => (
                            <option key={name} value={name}>
                              {name}
                            </option>
                          ))}
                        </select>
                        {entityMissing && <div className="text-sm text-red-600 mt-1">Required</div>}
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">{label} field</label>
                        <select
                          value={resolvedFieldName}
                          required
                          disabled={!resolvedEntityName?.trim()}
                          onChange={(e) => updateFieldMapping(key, { fieldName: e.target.value })}
                          className={textInputClassName}
                        >
                          {resolvedFieldName &&
                          !fieldOptions.some((name) => normalizeValue(name) === normalizeValue(resolvedFieldName)) ? (
                            <option value={resolvedFieldName}>{resolvedFieldName}</option>
                          ) : null}
                          <option value="" disabled>
                            {resolvedEntityName?.trim() ? 'Select a field' : 'Select an entity first'}
                          </option>
                          {fieldOptions.map((name) => (
                            <option key={name} value={name}>
                              {name}
                            </option>
                          ))}
                        </select>
                        {fieldMissing && <div className="text-sm text-red-600 mt-1">Required</div>}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">Roles</label>
            <div className="flex flex-wrap gap-2 mb-3">
              {jwt.roles.map((r) => (
                <span
                  key={r}
                  className="inline-flex items-center gap-2 px-2 py-1 rounded-full border border-gray-200 bg-gray-50 text-sm"
                >
                  <span>{r}</span>
                  <button
                    type="button"
                    className="text-gray-500 hover:text-gray-800"
                    onClick={() => removeRole(r)}
                    aria-label={`Remove role ${r}`}
                  >
                    ×
                  </button>
                </span>
              ))}
              {jwt.roles.length === 0 && <div className="text-sm text-gray-500">No roles yet</div>}
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={roleDraft}
                onChange={(e) => {
                  setRoleDraft(e.target.value);
                  if (roleError) setRoleError(null);
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    addRole();
                  }
                }}
                placeholder="Enter a role (e.g. USER), then press Enter"
                className={textInputClassName}
              />
              <button
                type="button"
                className="px-3 py-2 rounded-md border border-gray-200 text-sm hover:bg-gray-50"
                onClick={() => addRole()}
              >
                Add
              </button>
            </div>
            {roleError && <div className="text-sm text-red-600 mt-2">{roleError}</div>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Signing algorithm <RequiredAsterisk />
            </label>
            <select
              value={jwt.signingAlgorithm}
              onChange={(e) => updateSigningAlgorithm(e.target.value as JwtSigningAlgorithm)}
              className={textInputClassName}
            >
              {(['RS256', 'HS256'] as const).map((alg) => (
                <option key={alg} value={alg}>
                  {alg}
                </option>
              ))}
            </select>
            {jwt.signingAlgorithm === 'RS256' && (
              <div className="text-xs text-gray-500 mt-2">
                We will automatically generate the public key and private key for you.
              </div>
            )}
          </div>

          {jwt.signingAlgorithm === 'HS256' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                HS256 secret <RequiredAsterisk />
              </label>
              <input
                type="password"
                required
                value={jwt.hs256Secret ?? ''}
                onChange={(e) => setJwtConfig((current) => ({ ...current, hs256Secret: e.target.value }))}
                className={textInputClassName}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

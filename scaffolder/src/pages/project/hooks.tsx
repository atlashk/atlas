import { useCallback, useEffect, useMemo, useState } from 'react';
import { getStackOptions } from '../../api/client';
import { JwtConfig, ProjectConfig, StackOptionsResponse } from '../../models/types';
import { useProjectStore } from '../../store/projectStore';

type UseStackOptionsLoaderResult = {
  isLoadingStackOptions: boolean;
  stackOptionsError: string | null;
  loadStackOptions: () => void;
};

export const useStackOptionsLoader = (
  stackOptions: StackOptionsResponse | null,
  setStackOptions: (stackOptions: StackOptionsResponse) => void,
): UseStackOptionsLoaderResult => {
  const [isLoadingStackOptions, setIsLoadingStackOptions] = useState(false);
  const [stackOptionsError, setStackOptionsError] = useState<string | null>(null);

  const loadStackOptions = useCallback(() => {
    if (stackOptions || isLoadingStackOptions) return;
    let cancelled = false;
    setStackOptionsError(null);
    setIsLoadingStackOptions(true);
    void (async () => {
      try {
        const next = await getStackOptions();
        if (!cancelled) setStackOptions(next);
      } catch {
        if (!cancelled) setStackOptionsError('Failed to load stack options from backend');
      } finally {
        if (!cancelled) setIsLoadingStackOptions(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [isLoadingStackOptions, setStackOptions, stackOptions]);

  useEffect(() => loadStackOptions(), [loadStackOptions]);

  return { isLoadingStackOptions, stackOptionsError, loadStackOptions };
};

type UseJwtConfigControllerResult = {
  jwt: JwtConfig;
  setJwtConfig: (updater: (current: JwtConfig) => JwtConfig) => void;
};

export const useJwtConfigController = (
  config: ProjectConfig,
  setProjectInfo: (info: Partial<ProjectConfig>) => void,
): UseJwtConfigControllerResult => {
  const getDefaultJwtConfig = useCallback(
    (): JwtConfig => ({
      roles: ['USER', 'ADMIN'],
      accessTokenTtlSeconds: 86400,
      refreshTokenTtlSeconds: 2592000,
      signingAlgorithm: 'HS256',
      fieldMapping: {
        username: { entityName: '', fieldName: '' },
        password: { entityName: '', fieldName: '' },
        role: { entityName: '', fieldName: '' },
      },
      clientTokenTransport: { type: 'authorization_header', headerName: 'Authorization', prefix: 'Bearer ' },
      hs256Secret: '',
    }),
    [],
  );

  const sanitizeJwtFieldMapping = useCallback(
    (currentFieldMapping: Partial<JwtConfig['fieldMapping']> | undefined): JwtConfig['fieldMapping'] => {
      const defaults = getDefaultJwtConfig().fieldMapping;
      return {
        username: {
          entityName: currentFieldMapping?.username?.entityName ?? defaults.username.entityName,
          fieldName: currentFieldMapping?.username?.fieldName ?? defaults.username.fieldName,
        },
        password: {
          entityName: currentFieldMapping?.password?.entityName ?? defaults.password.entityName,
          fieldName: currentFieldMapping?.password?.fieldName ?? defaults.password.fieldName,
        },
        role: {
          entityName: currentFieldMapping?.role?.entityName ?? defaults.role.entityName,
          fieldName: currentFieldMapping?.role?.fieldName ?? defaults.role.fieldName,
        },
      };
    },
    [getDefaultJwtConfig],
  );

  const ensureJwtConfig = useCallback(() => {
    if (config.stack.authentication !== 'jwt') return;
    if (!config.jwt) {
      setProjectInfo({ jwt: getDefaultJwtConfig() });
      return;
    }

    const sanitizedFieldMapping = sanitizeJwtFieldMapping(config.jwt.fieldMapping);
    if (
      !config.jwt.fieldMapping ||
      config.jwt.fieldMapping.username.entityName !== sanitizedFieldMapping.username.entityName ||
      config.jwt.fieldMapping.username.fieldName !== sanitizedFieldMapping.username.fieldName ||
      config.jwt.fieldMapping.password.entityName !== sanitizedFieldMapping.password.entityName ||
      config.jwt.fieldMapping.password.fieldName !== sanitizedFieldMapping.password.fieldName ||
      config.jwt.fieldMapping.role.entityName !== sanitizedFieldMapping.role.entityName ||
      config.jwt.fieldMapping.role.fieldName !== sanitizedFieldMapping.role.fieldName
    ) {
      setProjectInfo({ jwt: { ...config.jwt, fieldMapping: sanitizedFieldMapping } });
    }
  }, [config.jwt, config.stack.authentication, getDefaultJwtConfig, sanitizeJwtFieldMapping, setProjectInfo]);

  useEffect(() => ensureJwtConfig(), [ensureJwtConfig]);

  const setJwtConfig = useCallback(
    (updater: (current: JwtConfig) => JwtConfig) => {
      const current = useProjectStore.getState().config.jwt ?? getDefaultJwtConfig();
      const next = updater(current);
      setProjectInfo({ jwt: { ...next, fieldMapping: sanitizeJwtFieldMapping(next.fieldMapping) } });
    },
    [getDefaultJwtConfig, sanitizeJwtFieldMapping, setProjectInfo],
  );

  const jwt = useMemo(() => config.jwt ?? getDefaultJwtConfig(), [config.jwt, getDefaultJwtConfig]);

  return { jwt, setJwtConfig };
};

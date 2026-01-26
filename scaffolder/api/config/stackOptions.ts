import { StackOptionsResponse } from '../models/types';

export const stackOptions: StackOptionsResponse = {
  databases: [
    { id: 'postgresql', label: 'PostgreSQL' },
    { id: 'mysql', label: 'MySQL' },
  ],
  authentication: [
    { id: 'none', label: 'None' },
    { id: 'jwt', label: 'JWT' },
  ],
  migration: [
    { id: 'none', label: 'None' },
    { id: 'flyway', label: 'Flyway' },
  ],
  deployment: [
    { id: 'none', label: 'None', deployment: [] },
    { id: 'docker-compose', label: 'Docker Compose', deployment: ['docker-compose'] },
    { id: 'kubernetes-native', label: 'Kubernetes Native', deployment: ['kubernetes'] },
    { id: 'kubernetes-helm', label: 'Kubernetes + Helm', deployment: ['kubernetes', 'helm'] },
  ],
  cicd: [
    { id: 'none', label: 'None' },
    { id: 'jenkins', label: 'Jenkins' },
    { id: 'github-actions', label: 'GitHub Actions' },
  ],
  defaults: {
    database: 'postgresql',
    authentication: 'none',
    migration: 'none',
    deployment: [],
    ci: 'none',
  },
};


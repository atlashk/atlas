import React from 'react';

export type TtlUnit = 'seconds' | 'minutes' | 'hours' | 'days';

export const cardClassName = 'bg-white p-6 rounded-xl border border-gray-200 shadow-sm';
export const textInputClassName =
  'w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500';

export const RequiredAsterisk = () => React.createElement('span', { className: 'text-red-600' }, '*');

import { registerPlugin } from '@capacitor/core';

import type { DashpayModulePluginPlugin } from './definitions';

const DashpayModulePlugin = registerPlugin<DashpayModulePluginPlugin>('DashpayModulePlugin', {
  web: () => import('./web').then(m => new m.DashpayModulePluginWeb()),
});

export * from './definitions';
export { DashpayModulePlugin };

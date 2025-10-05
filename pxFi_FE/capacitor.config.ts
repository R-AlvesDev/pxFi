import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
    appId: 'dev.ralves.pxfi',
    appName: 'pxFi',
    webDir: 'dist/px-fi-fe/browser',
    server: {
        androidScheme: 'http',
        cleartext: true
    }
};

export default config;

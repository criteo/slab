// @flow
import type { Status } from '../state';

export { fetcher } from './fetcher';
export * from './api';

export const statusToColor = (status: Status): string => {
  switch(status) {
    case 'SUCCESS': return '#2ebd59';
    case 'WARNING': return '#fdb843';
    case 'ERROR': return '#e7624f';
    default: return '#e0e0e0';
  }
};
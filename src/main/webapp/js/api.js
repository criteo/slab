/* @flow */
import { fetcher } from './utils';

export const fetchBoard = (board: string): Promise<Object | string> =>
  fetcher(`/api/boards/${board}`)
    .then(
      ({ body }) => body,
      ({ body }) => Promise.reject(body || 'network error')
    );

export const fetchLayout = (board: string): Promise<Object | string> =>
  fetcher(`/api/layouts/${board}`)
    .then(
      ({ body }) => body,
      ({ body }) => Promise.reject(body || 'network error')
    );
// @flow
import { fetcher } from './utils';

export const fetchBoard = (board: string): Promise<Object | string> =>
  fetcher(`/api/boards/${board}`)
    .then(
      ({ body }) => body,
      ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
    );

export const fetchBoards = (): Promise<Object | string> =>
  fetcher('/api/boards')
    .then(
      ({ body }) => body,
      ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
    );
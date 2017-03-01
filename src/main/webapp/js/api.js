// @flow
import { fetcher } from './utils';

export const fetchBoard = (board: string): Promise<Object | string> =>
  fetcher(`/api/boards/${board}`)
    .then(
      ({ body }) => body,
      ({ body, status }) => Promise.reject(body || `connection error ${status}`)
    );
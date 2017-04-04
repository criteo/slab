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

export const fetchTimeSeries = (board: string, box: string, from: number, until: number): Promise<Object | string> =>
  fetcher(`/api/boards/${board}/${box}/timeseries?from=${from}&until=${until}`)
    .then(
      ({ body }) => body,
      ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
    );

export const fetchHistory = (board: string): Promise<Object | string> =>
  fetcher(`/api/boards/${board}/history?last`)
    .then(
      ({ body }) => body,
      ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
    );
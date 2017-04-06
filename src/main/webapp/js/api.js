// @flow
import moment from 'moment';
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

export const fetchHistoryOfDay = (board: string, date: ?string): Promise<Object | string> => {
  const start = moment(date);
  if (!start.isValid())
    return Promise.reject(`invalid date ${date || ''}`);
  const end = start.clone().add(1, 'day');
  return fetcher(`/api/boards/${board}/history?from=${start.valueOf()}&until=${end.valueOf()}`)
    .then(
      ({ body }) => body,
      ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
    );
};

// @flow
import moment from 'moment';
import { fetcher } from './utils';

const handleResponse = (res: Promise<Object>): Promise<Object | string> =>
res
  .then(
    ({ body }) => body,
    ({ body, status = 0 }) => Promise.reject(body || `connection error ${status}`)
  );


export const fetchBoard = (board: string): Promise<Object | string> =>
  handleResponse(fetcher(`/api/boards/${board}`));

export const fetchBoards = (): Promise<Object | string> =>
  handleResponse(fetcher('/api/boards'));

export const fetchHistory = (board: string): Promise<Object | string> =>
  handleResponse(fetcher(`/api/boards/${board}/history?last`));

export const fetchHistoryOfDay = (board: string, date: ?string): Promise<Object | string> => {
  const start = moment(date);
  if (!start.isValid())
    return Promise.reject(`invalid date ${date || ''}`);
  const end = start.clone().add(1, 'day');
  return handleResponse(fetcher(`/api/boards/${board}/history?from=${start.valueOf()}&until=${end.valueOf()}`));
};

export const fetchSnapshot = (board: string, timestamp: number): Promise<Object | string> =>
  handleResponse(fetcher(`/api/boards/${board}/snapshot/${timestamp}`));

export const fetchStats = (board: string): Promise<Object | string> =>
  handleResponse(fetcher(`/api/boards/${board}/stats`));
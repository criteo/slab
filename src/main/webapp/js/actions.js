// @flow
import type { BoardView, TimeSeries, BoardConfig } from './state';

export type Action =
  FETCH_BOARD | FETCH_BOARD_SUCCESS | FETCH_BOARD_FAILURE | FETCH_BOARDS | FETCH_BOARDS_SUCCESS | FETCH_BOARDS_FAILURE
  | FETCH_TIME_SERIES | FETCH_TIME_SERIES_SUCCESS | FETCH_TIME_SERIES_FAILURE
  | FETCH_HISTORY | FETCH_HISTORY_SUCCESS | FETCH_HISTORY_FAILURE
  | SWITCH_BOARD_VIEW
  | SET_POLLING_INTERVAL;

export type FETCH_BOARD = { type: 'FETCH_BOARD', board: string };
export type FETCH_BOARD_SUCCESS = { type: 'FETCH_BOARD_SUCCESS', payload: BoardView };
export type FETCH_BOARD_FAILURE = { type: 'FETCH_BOARD_FAILURE', payload: string };

export function fetchBoard(board: string): FETCH_BOARD {
  return {
    type: 'FETCH_BOARD',
    board
  };
}

export type FETCH_BOARDS = { type: 'FETCH_BOARDS' };
export type FETCH_BOARDS_SUCCESS = { type: 'FETCH_BOARDS_SUCCESS', payload: Array<BoardConfig> };
export type FETCH_BOARDS_FAILURE = { type: 'FETCH_BOARDS_FAILURE', payload: string };

export function fetchBoards(): FETCH_BOARDS {
  return {
    type: 'FETCH_BOARDS'
  };
}

export type FETCH_TIME_SERIES = { type: 'FETCH_TIME_SERIES', board: string, box: string, from: number, until: number };
export type FETCH_TIME_SERIES_SUCCESS = { type: 'FETCH_TIME_SERIES_SUCCESS', payload: Array<TimeSeries> };
export type FETCH_TIME_SERIES_FAILURE = { type: 'FETCH_TIME_SERIES_FAILURE', payload: string };

export function fetchTimeSeries(board: string, box: string, from: number, until: number): FETCH_TIME_SERIES {
  return {
    type: 'FETCH_TIME_SERIES',
    board,
    box,
    from,
    until
  };
}

export type FETCH_HISTORY = { type: 'FETCH_HISTORY', date: ?string };
export type FETCH_HISTORY_SUCCESS = { type: 'FETCH_HISTORY_SUCCESS', board: string, payload: Array<any> };
export type FETCH_HISTORY_FAILURE = { type: 'FETCH_HISTORY_FAILURE', payload: string };

export function fetchHistory(board: string, date: ?string): FETCH_HISTORY {
  return {
    type: 'FETCH_HISTORY',
    board,
    date
  };
}

export type SWITCH_BOARD_VIEW = { type: 'SWITCH_BOARD_VIEW', isLiveMode: boolean, timestamp: number };

export function switchBoardView(isLiveMode: boolean, timestamp: number = 0): SWITCH_BOARD_VIEW {
  return {
    type: 'SWITCH_BOARD_VIEW',
    isLiveMode,
    timestamp
  };
}

export type SET_POLLING_INTERVAL = { type: 'SET_POLLING_INTERVAL', interval: number };

export function setPollingInterval(interval: number): SET_POLLING_INTERVAL {
  return {
    type: 'SET_POLLING_INTERVAL',
    interval
  };
}
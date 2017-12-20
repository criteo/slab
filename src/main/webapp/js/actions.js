// @flow
import { navigate } from 'redux-url';
import type { BoardView, BoardConfig } from './state';

export type Action =
  FETCH_BOARD | FETCH_BOARD_SUCCESS | FETCH_BOARD_FAILURE
  | FETCH_BOARDS | FETCH_BOARDS_SUCCESS | FETCH_BOARDS_FAILURE
  | FETCH_HISTORY | FETCH_HISTORY_SUCCESS | FETCH_HISTORY_FAILURE
  | FETCH_SNAPSHOT | FETCH_SNAPSHOT_SUCCESS | FETCH_SNAPSHOT_FAILURE
  | GOTO_SNAPSHOT | GOTO_LIVE_BOARD
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

export type FETCH_HISTORY = { type: 'FETCH_HISTORY', board: string, date: ?string };
export type FETCH_HISTORY_SUCCESS = { type: 'FETCH_HISTORY_SUCCESS', board: string, payload: Array<any> };
export type FETCH_HISTORY_FAILURE = { type: 'FETCH_HISTORY_FAILURE', payload: string };

export function fetchHistory(board: string, date: ?string): FETCH_HISTORY {
  return {
    type: 'FETCH_HISTORY',
    board,
    date
  };
}

export type FETCH_STATS = { type: 'FETCH_STATS', board: string};
export type FETCH_STATS_SUCCESS = { type: 'FETCH_STATS_SUCCESS', payload: Object };
export type FETCH_STATS_FAILURE = { type: 'FETCH_STATS_FAILURE', payload: string };

export function fetchStats(board: string): FETCH_STATS {
  return {
    type: 'FETCH_STATS',
    board
  };
}

export type FETCH_SNAPSHOT = { type: 'FETCH_SNAPSHOT' };
export type FETCH_SNAPSHOT_SUCCESS = { type: 'FETCH_SNAPSHOT_SUCCESS', payload: Object };
export type FETCH_SNAPSHOT_FAILURE = { type: 'FETCH_SNAPSHOT_FAILURE', payload: string };

export type SET_POLLING_INTERVAL = { type: 'SET_POLLING_INTERVAL', interval: number };

export function setPollingInterval(interval: number): SET_POLLING_INTERVAL {
  return {
    type: 'SET_POLLING_INTERVAL',
    interval
  };
}

// Routes
export type GOTO_SNAPSHOT = { type: 'GOTO_SNAPSHOT', board: string, timestamp: number };

export function navigateToSnapshot(board: string, timestamp: number) {
  return navigate(`/${board}/snapshot/${timestamp}`);
}

export type GOTO_LIVE_BOARD = { type: 'GOTO_LIVE_BOARD', board: string };

export function navigateToLiveBoard(board: string) {
  return navigate(`/${board}`);
}

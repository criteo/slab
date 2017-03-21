// @flow
import type { Board, TimeSeries } from './state';

export type Action =
  FETCH_BOARD | FETCH_BOARD_SUCCESS | FETCH_BOARD_FAILURE | FETCH_BOARDS | FETCH_BOARDS_SUCCESS | FETCH_BOARDS_FAILURE
  | FETCH_TIME_SERIES | FETCH_TIME_SERIES_SUCCESS | FETCH_TIME_SERIES_FAILURE;

export type FETCH_BOARD = { type: 'FETCH_BOARD', board: string };
export type FETCH_BOARD_SUCCESS = { type: 'FETCH_BOARD_SUCCESS', payload: Board };
export type FETCH_BOARD_FAILURE = { type: 'FETCH_BOARD_FAILURE', payload: string };

export function fetchBoard(board: string): FETCH_BOARD {
  return {
    type: 'FETCH_BOARD',
    board
  };
}

export type FETCH_BOARDS = { type: 'FETCH_BOARDS' };
export type FETCH_BOARDS_SUCCESS = { type: 'FETCH_BOARDS_SUCCESS', payload: Array<string> };
export type FETCH_BOARDS_FAILURE = { type: 'FETCH_BOARDS_FAILURE', payload: string };

export function fetchBoards(): FETCH_BOARDS {
  return {
    type: 'FETCH_BOARDS'
  };
}

export type FETCH_TIME_SERIES = { type: 'FETCH_TIME_SERIES', board: string, box: string, from: number, until: number };
export type FETCH_TIME_SERIES_SUCCESS = { type: 'FETCH_TIME_SERIES_SUCCESS', payload: Array<TimeSeries> };
export type FETCH_TIME_SERIES_FAILURE = { type: 'FETCH_TIME_SERIES_FAILURE' };

export function fetchTimeSeries(board: string, box: string, from: number, until: number): FETCH_TIME_SERIES {
  return {
    type: 'FETCH_TIME_SERIES',
    board,
    box,
    from,
    until
  };
}
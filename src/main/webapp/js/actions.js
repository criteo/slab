// @flow
import type { Board } from './state';

export type Action = FETCH_BOARD | FETCH_BOARD_SUCCESS | FETCH_BOARD_FAILURE;

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
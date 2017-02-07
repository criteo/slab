/* @flow */
import type { Board } from './state';

export type Action = FETCH_BOARD | FETCH_BOARD_SUCCESS | FETCH_BOARD_FAILURE;

export type FETCH_BOARD = { type: 'FETCH_BOARD', board: string };
export type FETCH_BOARD_SUCCESS = { type: 'FETCH_BOARD_SUCCESS', payload: Board };
export type FETCH_BOARD_FAILURE = { type: 'FETCH_BOARD_FAILURE', payload: string };

export type S = '1' | '2';

export function fetchBoard(board: string): FETCH_BOARD {
  return {
    type: 'FETCH_BOARD',
    board
  };
}
/* @flow */

import type { Board } from './state'

export type Action = LOADING_BOARD | LOADED_BOARD
export type LOADING_BOARD = {type: 'LOADING_BOARD'}
export type LOADED_BOARD = {type: 'LOADED_BOARD', board: Board}
export type CANT_LOAD_BOARD = {type: 'CANT_LOAD_BOARD'}
export type ZOOM_CHECK = {type: 'ZOOM_CHECK', check: string}

export function loading(): LOADING_BOARD {
  return {type: 'LOADING_BOARD'}
}

export function loaded(board: Board): LOADED_BOARD {
  return {type: 'LOADED_BOARD', board}
}

export function cantLoad(): CANT_LOAD_BOARD {
  return {type: 'CANT_LOAD_BOARD'}
}

export function zoomCheck(check: string): ZOOM_CHECK {
  return {type: 'ZOOM_CHECK', check}
}

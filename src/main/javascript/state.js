/* @flow */

import type { Action } from './actions'

export type Status = 'SUCCESS' | 'ERROR' | 'WARNING' | 'UNKNOWN'

export type Check = {
  id: string,
  status: Status,
  title: string,
  message: ?string,
  sub_checks: Array<Check>
}

export type Group = {
  id: number,
  subgroups: Array<SubGroup>
}

export type SubGroup = {
  id: number,
  name: string,
  checks: Array<Array<Check>>
}

export type SLA = {
  status: Status,
  message: string,
  checks: Array<Group>,
  dependencies: Array<[string,string]>
}

export type Board = {
  title: string,
  sla: SLA
}

export type State = {
  loading: boolean,
  board: ?Board,
  lostConnection: boolean
}

export function initialState(): State {
  return {
    loading: false,
    board: null,
    lostConnection: true
  }
}

// -- Reducers

export function reducers(currentState: State, action: Action): State {
  switch(action.type) {
    case 'LOADING_BOARD':
      return {
        ...currentState,
        loading: true
      }
    case 'LOADED_BOARD':
      return {
        loading: false,
        board: action.board,
        lostConnection: false
      }
    case 'CANT_LOAD_BOARD':
      return {
        ...currentState,
        loading: false,
        lostConnection: true
      }
    default:
      return currentState
  }
}

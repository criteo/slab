// @flow
import type { Action } from './actions';

export type Check = {
  title: string,
  status: Status,
  message: string,
  label: ?string
};

export type Box = {
  title: string,
  status: Status,
  message: string,
  description: ?string,
  labelLimit: number,
  checks: Array<Check>
};

export type Row = {
  title: string,
  percentage: number,
  boxes: Array<Box>
};

export type Column = {
  percentage: number,
  rows: Array<Row>
};

export type Board = {
  title: string,
  status: Status,
  message: string,
  columns: Array<Column>,
  links: Array<Link>
};

export type Link = [string, string];

type Status = 'Unknown' | 'Success' | 'Error' | 'Warning';

export type State = {
  isLoading: boolean,
  board: ?Board,
  error: ?string
};

const initState: State = {
  isLoading: false,
  board: null,
  error: null
};

export default function reducers(currentState: State = initState, action: Action): State {
  switch (action.type) {
    case 'FETCH_BOARD':
      return {
        ...currentState,
        isLoading: true
      };
    case 'FETCH_BOARD_SUCCESS':
      return {
        ...currentState,
        isLoading: false,
        board: action.payload
      };
    case 'FETCH_BOARD_FAILURE':
      return {
        ...currentState,
        isLoading: false,
        error: action.payload
      };
    default:
      return currentState;
  }
}

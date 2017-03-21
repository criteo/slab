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

export type TimeSeries = {
  title: string,
  data: Array<[number, number]>
}

export type Route = {
  path: string,
  [k: ?string]: any
};

export type Link = [string, string];

type Status = 'Unknown' | 'Success' | 'Error' | 'Warning';

export type State = {
  isLoading: boolean,
  isLoadingTimeSeries: boolean,
  board: ?Board,
  error: ?string,
  route: Route,
  boards: Array<string>,
  timeSeries: Array<TimeSeries>,
  timeSeriesError: ?string
};

const initState: State = {
  isLoading: false,
  isLoadingTimeSeries: false,
  board: null,
  error: null,
  route: {
    path: 'NOT_FOUND'
  },
  boards: [],
  timeSeries: [],
  timeSeriesError: null
};

export default function reducers(state: State = initState, action: Action): State {
  switch (action.type) {
    case 'FETCH_BOARD':
      return {
        ...state,
        isLoading: true
      };
    case 'FETCH_BOARD_SUCCESS':
      return {
        ...state,
        isLoading: false,
        board: action.payload
      };
    case 'FETCH_BOARD_FAILURE':
      return {
        ...state,
        isLoading: false,
        error: action.payload
      };
    case 'FETCH_BOARDS_SUCCESS':
      return {
        ...state,
        boards: action.payload
      };
    case 'FETCH_BOARDS_FAILURE':
      return {
        ...state,
        error: action.payload
      };
    case 'FETCH_TIME_SERIES':
      return {
        ...state,
        isLoadingTimeSeries: true
      };
    case 'FETCH_TIME_SERIES_SUCCESS':
      return {
        ...state,
        isLoadingTimeSeries: false,
        timeSeries: action.payload
      };
    case 'FETCH_TIME_SERIES_FAILURE':
      return {
        ...state,
        isLoadingTimeSeries: false,
        timeSeriesError: action.payload
      };
    case 'GOTO_BOARDS':
      return {
        ...state,
        route: {
          path: 'BOARDS'
        }
      };
    case 'GOTO_BOARD':
      return {
        ...state,
        route: {
          path: 'BOARD',
          board: action.payload.params.board
        }
      };
    case 'NOT_FOUND':
      return {
        ...state,
        route: {
          path: 'NOT_FOUND'
        }
      };
    default:
      return state;
  }
}

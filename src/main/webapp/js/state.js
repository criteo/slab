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

export type BoardView = {
  title: string,
  status: Status,
  message: string,
  columns: Array<Column>,
  links: Array<Link>,
  slo: number
};

export type Layout = {
  columns: Array<Column>
};

export type BoardConfig = {
  title: string,
  layout: Layout,
  links: Array<Link>
};

export type Stats = {
  [k: ?number]: number
};

export type StatsGroup = {
  daily: Stats,
  monthly: Stats,
  yearly: Stats
};

export type Route = {
  path: string,
  [k: ?string]: any
};

export type Link = [string, string];

type Status = 'Unknown' | 'Success' | 'Error' | 'Warning';

export type State = {
  currentBoard: ?string,
  route: Route,
  boards: Array<BoardConfig>,
  isLiveMode: boolean, // in live mode, polling the server
  history: {
    isLoading: boolean,
    data: any,
    error: ?string,
    date: ?string // if date is not specified, history comes from last 24 hours
  },
  liveBoardView: { // board view to be updated in live mode
    isLoading: boolean,
    data: ?BoardView,
    error: ?string
  },
  selectedBoardView: { // board view to be displayed
    isLoading: boolean,
    data: ?BoardView,
    error: ?string
  },
  selectedTimestamp: ?number, // timestamp of the snapshot board to be displayed
  stats: {
    isLoading: boolean,
    data: ?Object,
    error: ?string
  },
  pollingIntervalSeconds: number
};

const initState: State = {
  route: {
    path: 'NOT_FOUND'
  },
  currentBoard: null,
  boards: [],
  isLiveMode: true,
  history: {
    isLoading: false,
    data: null,
    error: null,
    date: null
  },
  liveBoardView: {
    isLoading: false,
    data: null,
    error: null
  },
  selectedBoardView: {
    isLoading: false,
    data: null,
    error: null,
  },
  stats: {
    isLoading: false,
    data: null,
    error: null
  },
  selectedTimestamp: null,
  pollingIntervalSeconds: 0
};

export default function reducers(state: State = initState, action: Action): State {
  switch (action.type) {
    // Current board view
    case 'FETCH_BOARD': {
      const liveBoardView = {
        ...state.liveBoardView,
        isLoading: true
      };
      return {
        ...state,
        liveBoardView,
        selectedBoardView: state.isLiveMode ? liveBoardView : state.selectedBoardView
      };
    }
    case 'FETCH_BOARD_SUCCESS': {
      const liveBoardView = {
        ...state.liveBoardView,
        isLoading: false,
        data: action.payload,
        error: null
      };
      return {
        ...state,
        liveBoardView,
        selectedBoardView: state.isLiveMode ? liveBoardView : state.selectedBoardView
      };
    }
    case 'FETCH_BOARD_FAILURE': {
      const liveBoardView = {
        ...state.liveBoardView,
        isLoading: false,
        error: action.payload
      };
      return {
        ...state,
        liveBoardView,
        selectedBoardView: state.isLiveMode ? liveBoardView : state.selectedBoardView
      };
    }
    // Boards
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
    // History
    case 'FETCH_HISTORY':
      return {
        ...state,
        history: {
          ...state.history,
          isLoading: true,
          date: action.date
        }
      };
    case 'FETCH_HISTORY_SUCCESS':
      return {
        ...state,
        history: {
          ...state.history,
          isLoading: false,
          error: null,
          data: action.payload
        }
      };
    case 'FETCH_HISTORY_FAILURE':
      return {
        ...state,
        history: {
          ...state.history,
          isLoading: false,
          error: action.payload
        }
      };
    // Snapshot
    case 'FETCH_SNAPSHOT_SUCCESS': {
      if (state.isLiveMode)
        return state;
      else
        return {
          ...state,
          selectedBoardView: {
            isLoading: false,
            error: null,
            data: action.payload
          }
        };
    }
    case 'FETCH_SNAPSHOT_FAILURE': {
      if (state.isLiveMode)
        return state;
      return {
        ...state,
        selectedBoardView: {
          isLoading: false,
          error: action.payload,
          data: state.selectedBoardView.data
        }
      };
    }
    // Stats
    case 'FETCH_STATS':
      return {
        ...state,
        stats: {
          ...state.stats,
          isLoading: true
        }
      };
    case 'FETCH_STATS_SUCCESS':
      return {
        ...state,
        stats: {
          ...state.stats,
          isLoading: false,
          data: action.payload,
          error: null
        }
      };
    case 'FETCH_STATS_FAILURE':
      return {
        ...state,
        stats: {
          ...state.stats,
          isLoading: true,
          data: null,
          error: action.payload
        }
      };
    // Polling service
    case 'SET_POLLING_INTERVAL':
      return {
        ...state,
        pollingIntervalSeconds: action.interval
      };
    // Routes
    case 'GOTO_BOARDS':
      return {
        ...state,
        currentBoard: null,
        route: {
          path: 'BOARDS'
        }
      };
    case 'GOTO_LIVE_BOARD':
      return {
        ...state,
        isLiveMode: true,
        selectedBoardView: {
          ...state.selectedBoardView,
          isLoading: true
        },
        selectedTimestamp: null,
        currentBoard: action.board,
        route: {
          path: 'BOARD',
          board: action.board
        }
      };
    // Time travel
    case 'GOTO_SNAPSHOT':
      return {
        ...state,
        isLiveMode: false,
        selectedBoardView: {
          ...state.selectedBoardView,
          isLoading: true
        },
        selectedTimestamp: action.timestamp,
        currentBoard: action.board,
        route: {
          path: 'BOARD',
          board: action.board
        }
      };
    case 'NOT_FOUND':
      return {
        ...state,
        currentBoard: null,
        route: {
          path: 'NOT_FOUND'
        }
      };
    default:
      return state;
  }
}

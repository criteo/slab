// @flow
import type { Action } from './actions';
import { combineViewAndLayout } from './utils';

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
  links: Array<Link>
};

export type Layout = {
  columns: Array<Column>
};

export type BoardConfig = {
  title: string,
  layout: Layout,
  links: Array<Link>
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
    // Time travel
    case 'SWITCH_BOARD_VIEW':
      if (action.isLiveMode === true)
        return {
          ...state,
          selectedBoardView: state.liveBoardView,
          isLiveMode: action.isLiveMode
        };
      else {
        const view = state.history.data[action.timestamp];
        const config = state.boards.find(_ => _.title === view.title);
        if (config)
          return {
            ...state,
            selectedBoardView: {
              isLoading: false,
              error: null,
              data: combineViewAndLayout(view, config.layout, config.links),
            },
            isLiveMode: action.isLiveMode,
            selectedTimestamp: action.timestamp
          };
        return state;
      }
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
    case 'GOTO_BOARD':
      return {
        ...state,
        currentBoard: action.payload.params.board,
        route: {
          path: 'BOARD',
          board: action.payload.params.board
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

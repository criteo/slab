import { takeLatest, call, put, fork, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { navigate } from 'redux-url';
import * as api from '../api';
import { combineViewAndLayout } from '../utils';
import { setPollingInterval } from '../actions';

// fetch the current view of the board
export function* fetchBoard(action, transformer = combineViewAndLayout) {
  try {
    const boards = yield call(fetchBoards);
    const boardView = yield call(api.fetchBoard, action.board);
    const config = boards.find(_ => _.title === boardView.title);
    const { layout, links } = config;
    yield put({ type: 'FETCH_BOARD_SUCCESS', payload: transformer(boardView, layout, links) });
  } catch (error) {
    yield put({ type: 'FETCH_BOARD_FAILURE', payload: error });
  }
}

export function* watchFetchBoard() {
  yield takeLatest('FETCH_BOARD', fetchBoard);
}

// fetch boards
export function* fetchBoards() {
  try {
    const cached = yield select(state => state.boards);
    const boards = cached && cached.length > 0 ? cached : yield call(api.fetchBoards);
    yield put({ type: 'FETCH_BOARDS_SUCCESS', payload: boards });
    return boards;
  } catch (error) {
    yield put({ type: 'FETCH_BOARDS_FAILURE', payload: error });
  }
}

export function* watchFetchBoards() {
  yield takeLatest('FETCH_BOARDS', fetchBoards);
}

// fetch history
export function* fetchHistory(action) {
  try {
    const history = yield call(
      action.date ? api.fetchHistoryOfDay : api.fetchHistory,
      action.board,
      action.date
    );
    yield put({ type: 'FETCH_HISTORY_SUCCESS', payload: history });
  } catch (error) {
    yield put({ type: 'FETCH_HISTORY_FAILURE', payload: error });
  }
}

export function* watchFetchHistory() {
  yield takeLatest('FETCH_HISTORY', fetchHistory);
}

// fetch snapshot
export function* fetchSnapshot(action) {
  try {
    if (action.isLiveMode)
      return;
    const currentBoard = yield select(state => state.currentBoard);
    const snapshot = yield call(api.fetchSnapshot, currentBoard, action.timestamp);
    yield put({ type: 'FETCH_SNAPSHOT_SUCCESS', payload: snapshot });
  } catch (error) {
    yield put({ type: 'FETCH_SNAPSHOT_FAILURE', payload: error });
  }
}

export function* watchSwitchBoardView() {
  yield takeLatest('SWITCH_BOARD_VIEW', fetchSnapshot);
}

// fetch stats
export function* fetchStats(action) {
  try {
    const stats = yield call(api.fetchStats, action.board);
    yield put({ type: 'FETCH_STATS_SUCCESS', payload: stats });
  } catch (error) {
    yield put({ type: 'FETCH_HISTORY_FAILURE', payload: error });
  }
}

export function* watchFetchStats() {
  yield takeLatest('FETCH_STATS', fetchStats);
}

// polling service
export function* poll() {
  const { interval, isLiveMode, date } = yield select(state => ({
    interval: state.pollingIntervalSeconds,
    isLiveMode: state.isLiveMode,
    date: state.history.date
  }));
  if (interval > 0) {
    const route = yield select(state => state.route);
    if (route.path === 'BOARD' && route.board && isLiveMode) {
      yield fork(fetchBoard, { type: 'FETCH_BOARD', board: route.board });
      if (!date) // polling history only in last 24 hours mode
        yield fork(fetchHistory, { type: 'FETCH_HISTORY', board: route.board });
    }
    yield delay(interval * 1000);
    yield call(poll);
  }
}

export function* watchPollingIntervalChange() {
  yield takeLatest('SET_POLLING_INTERVAL', poll);
}

// root
export default function* rootSaga() {
  // watchers
  yield fork(watchFetchBoard);
  yield fork(watchFetchBoards);
  yield fork(watchFetchHistory);
  yield fork(watchFetchStats);
  yield fork(watchPollingIntervalChange);
  yield fork(watchSwitchBoardView);

  // initial setup
  yield put(navigate(location.pathname, true));
  yield put(setPollingInterval(60));
}

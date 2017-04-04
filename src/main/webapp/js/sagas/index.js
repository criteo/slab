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
    const board = yield call(api.fetchBoard, action.board);
    const config = boards.find(_ => _.title === board.view.title);
    const { layout, links } = config;
    yield put({ type: 'FETCH_BOARD_SUCCESS', payload: transformer(board.view, layout, links) });
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

// time series
export function* fetchTimeSeries(action) {
  try {
    const timeSeries = yield call(api.fetchTimeSeries, action.board, action.box, action.from, action.until);
    yield put({ type: 'FETCH_TIME_SERIES_SUCCESS', payload: timeSeries });
  } catch (error) {
    yield put({ type: 'FETCH_TIME_SERIES_FAILURE', payload: error });
  }
}

export function* watchFetchTimeSeries() {
  yield takeLatest('FETCH_TIME_SERIES', fetchTimeSeries);
}

// fetch last history
export function* fetchHistory(action) {
  try {
    const history = yield call(api.fetchHistory, action.board);
    yield put({ type: 'FETCH_HISTORY_SUCCESS', payload: history });
  } catch (error) {
    yield put({ type: 'FETCH_HISTORY_FAILURE', paylod: error });
  }
}

export function* watchFetchHistory() {
  yield takeLatest('FETCH_HISTORY', fetchHistory);
}

// polling service
export function* poll() {
  const interval = yield select(state => state.pollingIntervalSeconds);
  if (interval > 0) {
    const route = yield select(state => state.route);
    if (route.path === 'BOARD' && route.board)
      yield fork(fetchBoard, { type: 'FETCH_BOARD', board: route.board });
    yield delay(interval * 1000);
    yield call(poll);
  }
}

export function* watchPollingIntervalChange() {
  yield takeLatest('SET_POLLING_INTERVAL', poll);
}

// root
export default function* rootSaga() {
  yield fork(watchFetchBoard);
  yield fork(watchFetchBoards);
  yield fork(watchFetchTimeSeries);
  yield fork(watchFetchHistory);
  yield fork(watchPollingIntervalChange);

  // initial setup
  yield put(navigate(location.pathname, true));
  yield put(setPollingInterval(60));
}

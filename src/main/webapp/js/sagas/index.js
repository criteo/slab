import { takeLatest, call, put, fork, select } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { navigate } from 'redux-url';
import moment from 'moment';
import * as api from '../api';
import { combineViewAndLayout, aggregateStatsByDay, aggregateStatsByMonth, aggregateStatsByYear } from '../utils';
import { setPollingInterval } from '../actions';

// fetch the current view of the board
export function* fetchBoard(action, transformer = combineViewAndLayout) {
  try {
    const boards = yield call(fetchBoards);
    const boardView = yield call(api.fetchBoard, action.board);
    const config = boards.find(_ => _.title === boardView.title);
    const { layout, links, slo } = config;
    yield put({ type: 'FETCH_BOARD_SUCCESS', payload: transformer(boardView, layout, links, slo) });
  } catch (error) {
    yield put({ type: 'FETCH_BOARD_FAILURE', payload: error });
  }
}

// from route change
export function* watchLiveBoardChange() {
  yield takeLatest('GOTO_LIVE_BOARD', fetchBoard);
}

// fetch boards
export function* fetchBoards() {
  try {
    const cached = yield select(state => state.boards);
    if (cached && cached.length > 0)
      return cached;
    const boards = yield call(api.fetchBoards);
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
export function* fetchSnapshot(action, transformer = combineViewAndLayout) {
  try {
    if (action.isLiveMode)
      return;
    const boards = yield call(fetchBoards);
    const currentBoard = yield select(state => state.currentBoard);
    const snapshot = yield call(api.fetchSnapshot, currentBoard, action.timestamp);
    const config = boards.find(_ => _.title === snapshot.title);
    const { layout, links } = config;
    yield put({ type: 'FETCH_SNAPSHOT_SUCCESS', payload: transformer(snapshot, layout, links) });
  } catch (error) {
    yield put({ type: 'FETCH_SNAPSHOT_FAILURE', payload: error });
  }
}

// from route change
export function* watchSnapshotChange() {
  yield takeLatest('GOTO_SNAPSHOT', handleSnapshotChange);
}

export function* handleSnapshotChange(action) {
  // if history is not available, fetch it
  const history = yield select(state => state.history.data);
  if (!history) {
    const datetime = moment(action.timestamp);
    const date = moment().isSame(datetime, 'day') ? null : datetime.format('YYYY-MM-DD');
    yield put({ type: 'FETCH_HISTORY', board: action.board, date });
  }
  yield call(fetchSnapshot, action);
}

// fetch stats
export function* fetchStats(action) {
  try {
    const hourlyStats = yield call(api.fetchStats, action.board);
    const dailyStats = aggregateStatsByDay(hourlyStats);
    const monthlyStats = aggregateStatsByMonth(hourlyStats);
    const yearlyStats = aggregateStatsByYear(hourlyStats);

    const stats = { daily: dailyStats, monthly: monthlyStats, yearly: yearlyStats };

    yield put({ type: 'FETCH_STATS_SUCCESS', payload: stats });
  } catch (error) {
    yield put({ type: 'FETCH_HISTORY_FAILURE', payload: error });
  }
}

export function* watchFetchStats() {
  yield takeLatest('FETCH_STATS', handleFetchStats);
}

export function* handleFetchStats(action) {
  const stats = yield select(state => state.stats.data);
  if (!stats || !stats[action.startDate]) {
    yield call(fetchStats, action);
  }
}

// polling service
export function* poll(init = false) {
  const { interval, isLiveMode, date } = yield select(state => ({
    interval: state.pollingIntervalSeconds,
    isLiveMode: state.isLiveMode,
    date: state.history.date
  }));
  if (interval > 0) {
    const route = yield select(state => state.route);
    if (route.path === 'BOARD' && route.board && isLiveMode) {
      if (!init) // do not call it in initialization
        yield fork(fetchBoard, { type: 'FETCH_BOARD', board: route.board });
      if (!date) // polling history only in last 24 hours mode
        yield fork(fetchHistory, { type: 'FETCH_HISTORY', board: route.board });
    }
    yield delay(interval * 1000);
    yield call(poll);
  }
}

export function* watchPollingIntervalChange() {
  yield takeLatest('SET_POLLING_INTERVAL', poll, true);
}

// root
export default function* rootSaga() {
  // watchers
  yield fork(watchFetchBoards);
  yield fork(watchFetchHistory);
  yield fork(watchFetchStats);
  yield fork(watchSnapshotChange);
  yield fork(watchLiveBoardChange);
  yield fork(watchPollingIntervalChange);

  // initial setup
  yield put(navigate(location.pathname, true));
  yield put(setPollingInterval(60));
}

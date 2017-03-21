import { takeLatest, call, put, fork } from 'redux-saga/effects';
import * as api from '../api';

// board
export function* fetchBoard(action, transformer = combine) {
  try {
    const board = yield call(api.fetchBoard, action.board);
    yield put({ type: 'FETCH_BOARD_SUCCESS', payload: transformer(board) });
  } catch (error) {
    yield put({ type: 'FETCH_BOARD_FAILURE', payload: error });
  }
}

export function* watchFetchBoard() {
  yield takeLatest('FETCH_BOARD', fetchBoard);
}

// boards
export function* fetchBoards() {
  try {
    const boards = yield call(api.fetchBoards);
    yield put({ type: 'FETCH_BOARDS_SUCCESS', payload: boards });
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

export default function* rootSaga() {
  yield fork(watchFetchBoard);
  yield fork(watchFetchBoards);
  yield fork(watchFetchTimeSeries);
}

// transform board response, merge views with layout
export const combine = board => {
  const { view, layout, links } = board;
  const map = new Map();
  layout.columns.forEach((col, i) =>
    col.rows.forEach((row, j) =>
      row.boxes.forEach((box, k) => map.set(box, [i,j,k]))
    )
  );
  view.children.forEach(box => {
    const [i, j, k] = map.get(box.title);
    // mutate layout
    layout.columns[i].rows[j].boxes[k] = {
      title: box.title,
      status: box.status,
      message: box.message,
      checks: box.children,
      description: box.description,
      labelLimit: box.labelLimit
    };
  });
  return {
    ...layout,
    links,
    title: view.title,
    message: view.message,
    status: view.status
  };
};
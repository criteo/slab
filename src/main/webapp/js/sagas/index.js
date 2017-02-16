import { takeLatest, call, put, fork } from 'redux-saga/effects';
import * as api from '../api';

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

export default function* rootSaga() {
  yield fork(watchFetchBoard);
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
      description: box.description
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
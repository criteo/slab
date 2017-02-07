import { takeLatest, call, put, fork } from 'redux-saga/effects';
import * as api from '../api';

function* fetchBoard(action) {
  try {
    const res = yield [
      call(api.fetchBoard, action.board),
      call(api.fetchLayout, action.board)
    ];
    const [board, layout] = res;
    yield put({ type: 'FETCH_BOARD_SUCCESS', payload: combine(board, layout) });
  } catch (error) {
    yield put({ type: 'FETCH_BOARD_FAILURE', payload: error });
  }
}

function* watchFetchBoard() {
  yield takeLatest('FETCH_BOARD', fetchBoard);
}

export default function* rootSaga() {
  yield fork(watchFetchBoard);
}

// combine board views with layout
export const combine = (board, layout) => {
  const map = new Map();
  layout.columns.forEach((col, i) =>
    col.rows.forEach((row, j) =>
      row.boxes.forEach((box, k) => map.set(box, [i,j,k]))
    )
  );
  board.children.forEach(box => {
    const [i, j, k] = map.get(box.title);
    layout.columns[i].rows[j].boxes[k] = {
      title: box.title,
      status: box.status,
      message: box.message,
      checks: box.children
    };
  });
  return {
    ...layout,
    title: board.title,
    message: board.message,
    status: board.status
  };
};
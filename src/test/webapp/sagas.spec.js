import { call, put } from 'redux-saga/effects';
import * as api from 'src/api';
import { fetchBoard, fetchBoards } from 'src/sagas';
describe('saga spec', () => {

  describe('fetchBoard', () => {
    it('calls API', () => {
      const iter = fetchBoard({ board: 'boardname' }, view => view);
      expect(iter.next().value).to.deep.equal(
        call(fetchBoards)
      );
      expect(iter.next([{ layout: {}, links: [] }]).value).to.deep.equal(
        call(api.fetchBoard, 'boardname'),
      );
      const result = iter.next({ view: [1,2,3] }).value;
      expect(result).to.deep.equal(put({
        type: 'FETCH_BOARD_SUCCESS',
        payload: [1,2,3]
      }));
    });

    it('put failure action if any exception is raised', () => {
      const iter = fetchBoard({ board: 'boardname' });
      iter.next();
      const error = new Error('uh');
      const next = iter.throw(error);
      expect(next.value).to.deep.equal(put({ type: 'FETCH_BOARD_FAILURE', payload: error }));
    });
  });
});
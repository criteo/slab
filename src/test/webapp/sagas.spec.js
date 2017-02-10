import { call, put } from 'redux-saga/effects';
import * as api from 'src/api';
import { fetchBoard } from 'src/sagas';
describe('saga spec', () => {

  describe('fetchBoard', () => {
    it('calls API', () => {
      const iter = fetchBoard({ board: 'boardname' }, (...args) => args);
      expect(iter.next().value).to.deep.equal([
        call(api.fetchBoard, 'boardname'),
        call(api.fetchLayout, 'boardname')
      ]);
      const fetchResult = [1,2];
      expect(iter.next(fetchResult).value).to.deep.equal(put({
        type: 'FETCH_BOARD_SUCCESS',
        payload: fetchResult
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